package com.hp.application.automation.tools.run;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.junit.JUnitParser;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestResult;
import hudson.util.ArgumentListBuilder;
import jenkins.MasterToSlaveFileCallable;
import jenkins.SlaveToMasterFileCallable;
import jenkins.model.Jenkins;
import jenkins.security.Roles;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

/**
 * Created by kazaky on 14/03/2017.
 */
public class RunLRScript extends Builder implements SimpleBuildStep {
    public static final String LINUX_MDRV_PATH = "/bin/mdrv";
    public static final String WIN_MDRV_PATH = "\\bin\\mmdrv.exe";

    private final String _scriptsPath;
    private PrintStream logger;

    @DataBoundConstructor
    public RunLRScript(String scriptsPath) {
        this._scriptsPath = scriptsPath;
    }


    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {

        logger = listener.getLogger();
        ArgumentListBuilder args = new ArgumentListBuilder();
        FilePath mdrv;
        EnvVars env = null;
        try {
            env = build.getEnvironment(listener);
        } catch (IOException | InterruptedException e) {
            listener.error("Failed loading build environment " + e);
        }

        FilePath buildWorkDir = workspace.child(build.getId());


        //base command line -usr "%1\%1.usr" -extra_ext NVReportExt -qt_result_dir "c:\%1_results"
        //Do run the script on linux or windows?
        mdrv = getMDRVPath(launcher, env);
        args.add(mdrv);

        FilePath scriptPath = workspace.child(this._scriptsPath);
        args.add("-usr");
        args.add(scriptPath);
        args.add("-extra_ext NVReportExt");

        args.add("-qt_result_dir");
        buildWorkDir.mkdirs();
        buildWorkDir = buildWorkDir.absolutize();
        String scriptName = FilenameUtils.getBaseName(this._scriptsPath);
        if (Objects.equals(scriptName, "")) {
            //TODO: if "" it's folder and we need to scan it

        }
        FilePath scriptWorkDir = buildWorkDir.child(scriptName);
        scriptWorkDir.mkdirs();
        scriptWorkDir = scriptWorkDir.absolutize();
        args.add(scriptWorkDir);

        try {
            int returnCode = launcher.launch().cmds(args).stdout(logger).pwd(scriptWorkDir).join();
            if (returnCode != 0) {
                build.setResult(Result.FAILURE);
                return;
            }

        } catch (IllegalArgumentException e) {
            build.setResult(Result.FAILURE);
            logger.println(e);
        }

        FilePath masterBuildWorkspace = new FilePath(build.getRootDir());

        //This part is on the master - we convert the results to JUnit and HTML report using specialized XSLT
        final String resultXmlPath = buildWorkDir.child(scriptName).child("Results.xml").absolutize()
                .getRemote();
        if (resultXmlPath == null) {
            listener.fatalError(resultXmlPath + "not found in resources");
            return;
        }


        final String libLR = "\\lib\\LR\\PDetails.xsl";
        final String xsltPath = Jenkins.getInstance().pluginManager.uberClassLoader.getResource(libLR).getPath();


        FilePath outputHTML = buildWorkDir.child(scriptName);
        outputHTML.mkdirs();
        outputHTML = outputHTML.child("result.html");
        FilePath xsltOnNode = workspace.child("resultsHtml.xslt");
        xsltOnNode.copyFrom(Jenkins.getInstance().pluginManager.uberClassLoader.getResource(libLR));
        if (xsltOnNode.exists()) {
            logger.println("Found XSLT on slave");
        }
        createHtmlReports(buildWorkDir, scriptName, outputHTML, xsltOnNode);
        LrScriptResultsParser lrScriptResultsParser = new LrScriptResultsParser();
        lrScriptResultsParser.parseScriptResult(scriptName, build, buildWorkDir, launcher, listener);
        copyScriptsResultToMaster(build, listener, buildWorkDir, masterBuildWorkspace);
        Thread.sleep(4000);

        JUnitResultArchiver jUnitResultArchiver = new JUnitResultArchiver("JunitResult.xml");
        jUnitResultArchiver.setKeepLongStdio(true);
        jUnitResultArchiver.setAllowEmptyResults(true);
        jUnitResultArchiver.perform(build, buildWorkDir.child(scriptName), launcher, listener);


        build.setResult(Result.SUCCESS);
    }

    private void createHtmlReports(FilePath buildWorkDir, String scriptName, FilePath outputHTML, FilePath xsltOnNode)
            throws IOException, InterruptedException {
        //TODO: check arguments
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            StreamSource xslStream = new StreamSource(xsltOnNode.read());
            Transformer transformer = factory.newTransformer(xslStream);
            StreamSource in = new StreamSource(buildWorkDir.child(scriptName).child("Results.xml").read());
            StreamResult out = new StreamResult(outputHTML.write());
            transformer.transform(in, out);
            logger.println("The generated HTML file is:" + outputHTML);
        } catch (TransformerConfigurationException e) {
            logger.println("TransformerConfigurationException");
            logger.println(e);
        } catch (TransformerException e) {
            logger.println("TransformerException");
            logger.println(e);
        }
    }

    private void copyScriptsResultToMaster(@Nonnull Run<?, ?> build, @Nonnull TaskListener listener,
                                           FilePath buildWorkDir, FilePath masterBuildWorkspace)
            throws IOException, InterruptedException {
        listener.getLogger().printf("Copying script results, from '%s' on '%s' to '%s' on the master. %n"
                , buildWorkDir.toURI(), Computer.currentComputer().getNode(), build.getRootDir().toURI());

        buildWorkDir.copyRecursiveTo(masterBuildWorkspace);
    }

    private FilePath getMDRVPath(@Nonnull Launcher launcher, EnvVars env) throws IllegalArgumentException {
        FilePath mdrv;
        if (launcher.isUnix()) {
            String lrPath = env.get("M_LROOT", "");
            if (Objects.equals(lrPath, "")) {
                throw new IllegalArgumentException(
                        "Please make sure environment variables are set correctly on the running node - " +
                                "LR_PATH for windows and M_LROOT for linux");
            }
            lrPath += LINUX_MDRV_PATH;
            mdrv = new FilePath(launcher.getChannel(), lrPath);
        } else {
            String lrPath = env.get("LR_PATH", "");
            if (Objects.equals(lrPath, "")) {
                throw new IllegalArgumentException("Please make sure enviroment varibals are set correctly on the " +
                        "running node - " +
                        "LR_PATH for windows and M_LROOT for linux");
            }
            lrPath += WIN_MDRV_PATH;
            mdrv = new FilePath(launcher.getChannel(), lrPath);
        }
        return mdrv;
    }


    public String getScriptsPath() {
        return _scriptsPath;
    }

    @Symbol("RunLoadRunnerScript")
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {
        private String global;

        public Descriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Run LoadRunner script";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            global = formData.getString("scriptsPath");
            save();
            return super.configure(req, formData);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

    }

}
