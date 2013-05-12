package com.hp.application.automation.tools.run;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.hp.application.automation.tools.model.AlmServerSettingsModel;
import com.hp.application.automation.tools.model.CdaDetails;
import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.SseModel;
import com.hp.application.automation.tools.settings.AlmServerSettingsBuilder;
import com.hp.application.automation.tools.sse.SSEBuilderPerformer;
import com.hp.application.automation.tools.sse.result.model.junit.Testcase;
import com.hp.application.automation.tools.sse.result.model.junit.Testsuite;
import com.hp.application.automation.tools.sse.result.model.junit.Testsuites;
import com.hp.application.automation.tools.sse.sdk.Logger;

/***
 * This Jenkins plugin contains an unofficial implementation of some of the elements of the HP ALM
 * Lab Management SDK. Users are free to use this plugin as they wish, but HP does not take
 * responsibility for supporting or providing backwards compatibility for the functionality herein.
 * 
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class SseBuilder extends Builder {
    
    private final SseModel _sseModel;
    private String _fileName;
    
    @DataBoundConstructor
    public SseBuilder(
            String almServerName,
            String almUserName,
            String almPassword,
            String almDomain,
            String almProject,
            String description,
            String runType,
            String almEntityId,
            String timeslotDuration,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {
        
        _sseModel =
                new SseModel(
                        almServerName,
                        almUserName,
                        almPassword,
                        almDomain,
                        almProject,
                        runType,
                        almEntityId,
                        timeslotDuration,
                        description,
                        postRunAction,
                        environmentConfigurationId,
                        cdaDetails);
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        
        return (DescriptorImpl) super.getDescriptor();
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        
        Result resultStatus = Result.FAILURE;
        _sseModel.setAlmServerUrl(getServerUrl(_sseModel.getAlmServerName()));
        PrintStream logger = listener.getLogger();
        Testsuites testsuites = execute(build, logger);
        
        FilePath resultsFilePath = build.getWorkspace().child(getFileName());
        resultStatus = createRunResults(resultsFilePath, testsuites, logger);
        provideStepResultStatus(resultStatus, build, logger);
        
        return true;
    }
    
    public AlmServerSettingsModel getAlmServerSettingsModel() {
        
        AlmServerSettingsModel ret = null;
        for (AlmServerSettingsModel almServer : getDescriptor().getAlmServers()) {
            if (_sseModel != null
                && _sseModel.getAlmServerName().equals(almServer.getAlmServerName())) {
                ret = almServer;
                break;
            }
        }
        
        return ret;
    }
    
    private void provideStepResultStatus(
            Result resultStatus,
            AbstractBuild<?, ?> build,
            PrintStream logger) {
        
        logger.println(String.format("Result Status: %s", resultStatus.toString()));
        build.setResult(resultStatus);
        
    }
    
    private Testsuites execute(AbstractBuild<?, ?> build, PrintStream logger) {
        
        Testsuites ret = null;
        SSEBuilderPerformer performer = null;
        try {
            performer = new SSEBuilderPerformer();
            ret = execute(performer, logger);
        } catch (InterruptedException e) {
            build.setResult(Result.ABORTED);
            stop(performer, logger);
        } catch (Throwable cause) {
            build.setResult(Result.FAILURE);
        }
        
        return ret;
    }
    
    private Result createRunResults(FilePath filePath, Testsuites testsuites, PrintStream logger) {
        
        Result ret = Result.SUCCESS;
        try {
            if (testsuites != null) {
                StringWriter writer = new StringWriter();
                JAXBContext context = JAXBContext.newInstance(Testsuites.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(testsuites, writer);
                filePath.write(writer.toString(), null);
                if (containsErrors(testsuites.getTestsuite())) {
                    ret = Result.UNSTABLE;
                }
            } else {
                logger.println("Empty Results");
                ret = Result.UNSTABLE;
            }
            
        } catch (Throwable cause) {
            logger.print(String.format(
                    "Failed to create run results, Exception: %s",
                    cause.getMessage()));
            ret = Result.UNSTABLE;
        }
        
        return ret;
    }
    
    private boolean containsErrors(List<Testsuite> testsuites) {
        
        boolean ret = false;
        for (Testsuite testsuite : testsuites) {
            for (Testcase testcase : testsuite.getTestcase()) {
                if (testcase.getStatus().equals("error")) {
                    ret = true;
                    break;
                }
            }
        }
        
        return ret;
    }
    
    private String getFileName() {
        
        Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        String time = formatter.format(new Date());
        _fileName = String.format("Results%s.xml", time);
        return _fileName;
    }
    
    private void stop(SSEBuilderPerformer performer, PrintStream logger) {
        
        try {
            if (performer != null) {
                performer.stop();
            }
        } catch (Throwable cause) {
            logger.println(String.format("Failed to stop BVS. Exception: %s", cause.getMessage()));
        }
    }
    
    private Testsuites execute(SSEBuilderPerformer performer, final PrintStream logger)
            throws InterruptedException, IOException {
        
        return performer.start(_sseModel, new Logger() {
            
            @Override
            public void log(String message) {
                
                logger.println(message);
            }
        });
    }
    
    public String getServerUrl(String almServerName) {
        
        String ret = "";
        AlmServerSettingsModel[] almServers = getDescriptor().getAlmServers();
        if (almServers != null && almServers.length > 0) {
            for (AlmServerSettingsModel almServer : almServers) {
                if (almServerName.equals(almServer.getAlmServerName())) {
                    ret = almServer.getAlmServerUrl();
                    break;
                }
            }
        }
        
        return ret;
    }
    
    public SseModel getSseModel() {
        
        return _sseModel;
    }
    
    public String getRunResultsFileName() {
        
        return _fileName;
    }
    
    // This indicates to Jenkins that this is an implementation of an extension point
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        
        public DescriptorImpl() {
            
            load();
        }
        
        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            
            return true;
        }
        
        @Override
        public String getDisplayName() {
            
            return "Execute HP tests using HP ALM Lab Management";
        }
        
        public boolean hasAlmServers() {
            
            return Hudson.getInstance().getDescriptorByType(
                    AlmServerSettingsBuilder.DescriptorImpl.class).hasAlmServers();
        }
        
        public AlmServerSettingsModel[] getAlmServers() {
            
            return Hudson.getInstance().getDescriptorByType(
                    AlmServerSettingsBuilder.DescriptorImpl.class).getInstallations();
        }
        
        public FormValidation doCheckAlmUserName(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("User name must be set");
            }
            
            return ret;
        }
        
        public FormValidation doCheckTimeslotDuration(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Timeslot duration must be set");
            } else if (Integer.valueOf(value) < 30) {
                ret = FormValidation.error("Timeslot duration must be higher than 30");
            }
            
            return ret;
        }
        
        public FormValidation doCheckAlmDomain(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Domain must be set");
            }
            
            return ret;
        }
        
        public FormValidation doCheckAlmProject(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Project must be set");
            }
            
            return ret;
        }
        
        public FormValidation doCheckAlmEntityId(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Entity ID must be set.");
            }
            
            return ret;
        }
        
        public List<EnumDescription> getRunTypes() {
            
            return SseModel.getRunTypes();
        }
        
        public List<EnumDescription> getPostRunActions() {
            
            return SseModel.getPostRunActions();
        }
        
        public List<EnumDescription> getDeploymentActions() {
            
            return CdaDetails.getDeploymentActions();
        }
        
        public static List<EnumDescription> getDeprovisioningActions() {
            
            return CdaDetails.getDeprovisioningActions();
        }
    }
}
