package com.hp.application.automation.tools.octane.executor;

import antlr.ANTLRException;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import com.hp.application.automation.tools.octane.actions.UFTTestDetectionPublisher;
import com.hp.application.automation.tools.results.RunResultRecorder;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import com.hp.octane.integrations.dto.executor.DiscoveryInfo;
import com.hp.octane.integrations.dto.executor.TestExecutionInfo;
import com.hp.octane.integrations.dto.executor.TestSuiteExecutionInfo;
import com.hp.octane.integrations.dto.executor.impl.TestingToolType;
import com.hp.octane.integrations.dto.scm.SCMRepository;
import com.hp.octane.integrations.dto.scm.SCMType;
import hudson.model.*;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.UserRemoteConfig;
import hudson.tasks.LogRotator;
import hudson.triggers.SCMTrigger;
import jenkins.model.BuildDiscarder;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * This service is responsible to create jobs (discovery and execution) for execution process.
 * Created by berkovir on 20/03/2017.
 */
public class TestExecutionJobCreatorService {


    private static final Logger logger = LogManager.getLogger(TestExecutionJobCreatorService.class);
    public static final String EXECUTOR_ID_PARAMETER_NAME = "executorId";
    public static final String SUITE_ID_PARAMETER_NAME = "suiteId";
    public static final String SUITE_RUN_ID_PARAMETER_NAME = "suiteRunId";

    public static void runTestSuiteExecution(TestSuiteExecutionInfo suiteExecutionInfo) {

        /*
        {
                "tests": [{
                        "testName": "GUITest2",
                        "packageName": "GUITests"
                    }, {
                        "testName": "GUITest3",
                        "packageName": "GUITests"
                    }
                ],
                "scmRepository": {
                    "type": "git",
                                            "url": "git@github.com:radislavB/UftTests.git"
                },
                "executorId": "1",
                "workspaceId": "1002",
                "suiteId": "6",
                "testingToolType": "uft"
            }
         */
        FreeStyleProject proj = getExecutionJob(suiteExecutionInfo);

        //start job
        if (proj != null) {
            ParameterValue suiteRunIdParam = new StringParameterValue(SUITE_RUN_ID_PARAMETER_NAME, suiteExecutionInfo.getSuiteRunId());
            ParameterValue suiteIdParam = new StringParameterValue(SUITE_ID_PARAMETER_NAME, suiteExecutionInfo.getSuiteId());
            ParametersAction parameters = new ParametersAction(suiteRunIdParam, suiteIdParam);

            Cause cause = StringUtils.isNotEmpty(suiteExecutionInfo.getSuiteRunId()) ? TriggeredBySuiteRunCause.create(suiteExecutionInfo.getSuiteRunId()) : new Cause.UserIdCause();
            CauseAction causeAction = new CauseAction(cause);
            proj.scheduleBuild2(0, parameters, causeAction);
        }
    }

    private static FreeStyleProject getExecutionJob(TestSuiteExecutionInfo suiteExecutionInfo) {

        try {
            String projectName = String.format("%s test execution job - suiteId %s",
                    suiteExecutionInfo.getTestingToolType().toString(),
                    suiteExecutionInfo.getSuiteId());

            //validate creation of job
            FreeStyleProject proj = (FreeStyleProject) Jenkins.getInstance().getItem(projectName);
            if (proj == null) {
                proj = Jenkins.getInstance().createProject(FreeStyleProject.class, projectName);
                proj.setDescription(String.format("This job was created by HP AA Plugin for execution of %s tests, as part of Octane suite with id %s",
                        suiteExecutionInfo.getTestingToolType().toString(), suiteExecutionInfo.getSuiteId()));
            }

            setScmRepository(suiteExecutionInfo.getScmRepository(), suiteExecutionInfo.getScmRepositoryCredentialsId(), proj);
            setBuildDiscarder(proj, 20);
            addParameter(proj, SUITE_ID_PARAMETER_NAME, suiteExecutionInfo.getSuiteId(), true, "Octane suite id");
            addParameter(proj, SUITE_RUN_ID_PARAMETER_NAME, null, false, "This parameter is relevant only if the suite is run from Octane and allows to publish tests to the existing Octane suite run");
            addAssignedNode(proj);

            //add build action
            String fsTestsData = prepareMtbxData(suiteExecutionInfo.getTests());
            List<RunFromFileBuilder> builders = proj.getBuildersList().getAll(RunFromFileBuilder.class);
            if (builders != null && !builders.isEmpty()) {
                builders.get(0).setFsTests(fsTestsData);
            } else {
                proj.getBuildersList().add(new RunFromFileBuilder(fsTestsData));
            }

            //add post-build action - publisher
            RunResultRecorder runResultRecorder = null;
            List publishers = proj.getPublishersList();//.add(new RunResultRecorder(ResultsPublisherModel.alwaysArchiveResults.getValue()));
            for (Object publisher : publishers) {
                if (publisher instanceof RunResultRecorder) {
                    runResultRecorder = (RunResultRecorder) publisher;
                }
            }
            if (runResultRecorder == null) {
                runResultRecorder = new RunResultRecorder(ResultsPublisherModel.alwaysArchiveResults.getValue());
                publishers.add(runResultRecorder);
            }
            return proj;
        } catch (IOException e) {
            logger.error("Failed to create ExecutionJob : " + e.getMessage());
            return null;
        }
    }

    private static void setScmRepository(SCMRepository scmRepository, String scmRepositoryCredentialsId, FreeStyleProject proj) {
        if (SCMType.GIT.equals(scmRepository.getType())) {
            try {

                List<UserRemoteConfig> repoLists = Arrays.asList(new UserRemoteConfig(scmRepository.getUrl(), null, null, scmRepositoryCredentialsId));
                GitSCM scm = new GitSCM(repoLists, Collections.singletonList(new BranchSpec("")), Boolean.valueOf(false), Collections.<SubmoduleConfig>emptyList(), null, null, null);

                //GitSCM scm = new GitSCM(scmRepository.getUrl());
                proj.setScm(scm);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to set Git repository : " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("SCM repository " + scmRepository.getType() + " isn't supported yet");
        }
    }

    private static String prepareMtbxData(List<TestExecutionInfo> tests) throws IOException {
        /*<Mtbx>
            <Test name="test1" path="c:\tests\APITest1">
			<Parameter name="A" value="abc" type="string"/>
			 ….
			</Test>
			<Test name="test2" path="${WORKSPACE}\test2">
				<Parameter name="p1" value="123" type="int"/>
				<Parameter name="p4" value="123.4" type="float"/>
			….
			</Test>
		</Mtbx>*/

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("mtbx");
            doc.appendChild(rootElement);

            for (TestExecutionInfo test : tests) {
                Element testElement = doc.createElement("test");
                testElement.setAttribute("name", test.getTestName());

                String path = "${WORKSPACE}" + File.separator + test.getPackageName() + (StringUtils.isEmpty(test.getPackageName()) ? "" : File.separator) + test.getTestName();
                testElement.setAttribute("path", path);
                rootElement.appendChild(testElement);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            String str = writer.toString();
            return str;
        } catch (Exception e) {
            throw new IOException("Failed to build MTBX content : " + e.getMessage());
        }

    }

    public static void runTestDiscovery(DiscoveryInfo discoveryInfo) {

        /*
        {
          "scmRepository": {
            "type": "git",
            "url": "git@github.com:radislavB/UftTests.git"
          },
          "executorId": "1",
          "workspaceId": "1002",
          "testingToolType": "uft",
          "forceFullDiscovery": true
        }
         */
        FreeStyleProject proj = getDiscoveryJob(discoveryInfo);

        if (proj != null) {
            if (discoveryInfo.isForceFullDiscovery() && proj.getWorkspace() != null) {
                UFTTestDetectionService.removeInitialDetectionFlag(proj.getWorkspace());
            }

            proj.scheduleBuild2(0);
        }
    }

    private static FreeStyleProject getDiscoveryJob(DiscoveryInfo discoveryInfo) {

        try {
            String discoveryJobName = buildDiscoveryJobName(discoveryInfo.getTestingToolType(), discoveryInfo.getExecutorId());
            //validate creation of job
            FreeStyleProject proj = (FreeStyleProject) Jenkins.getInstance().getItem(discoveryJobName);
            if (proj == null) {

                proj = Jenkins.getInstance().createProject(FreeStyleProject.class, discoveryJobName);
                proj.setDescription(String.format("This job was created by HP AA Plugin for discovery of %s tests, as part of Octane executor with id %s",
                        discoveryInfo.getTestingToolType().toString(), discoveryInfo.getExecutorId()));
            }

            setScmRepository(discoveryInfo.getScmRepository(), discoveryInfo.getScmRepositoryCredentialsId(), proj);
            setBuildDiscarder(proj, 20);
            addParameter(proj, EXECUTOR_ID_PARAMETER_NAME, discoveryInfo.getExecutorId(), true, "Octane executor id");

            //set polling once in two minutes
            SCMTrigger scmTrigger = new SCMTrigger("H/2 * * * *");//H/2 * * * * : once in two minutes
            proj.addTrigger(scmTrigger);
            delayPollingStart(proj, scmTrigger);


            //add post-build action - publisher
            UFTTestDetectionPublisher uftTestDetectionPublisher = null;
            List publishers = proj.getPublishersList();
            for (Object publisher : publishers) {
                if (publisher instanceof UFTTestDetectionPublisher) {
                    uftTestDetectionPublisher = (UFTTestDetectionPublisher) publisher;
                }
            }


            if (uftTestDetectionPublisher == null) {
                uftTestDetectionPublisher = new UFTTestDetectionPublisher(discoveryInfo.getWorkspaceId(), discoveryInfo.getScmRepositoryId());
                publishers.add(uftTestDetectionPublisher);
            }

            return proj;
        } catch (IOException | ANTLRException e) {
            logger.error("Failed to  create DiscoveryJob : " + e.getMessage());
            return null;
        }
    }

    private static String buildDiscoveryJobName(TestingToolType testingToolType, String executorId) {
        String name = String.format("%s test discovery job - executorId %s", testingToolType.toString(), executorId);
        return name;
    }

    private static void setBuildDiscarder(FreeStyleProject proj, int numBuildsToKeep) throws IOException {
        int IRRELEVANT = -1;
        BuildDiscarder bd = new LogRotator(IRRELEVANT, numBuildsToKeep, IRRELEVANT, IRRELEVANT);
        proj.setBuildDiscarder(bd);
    }

    /**
     * Delay starting of polling by 5 minutes to allow original clone
     *
     * @param proj
     * @param scmTrigger
     */
    private static void delayPollingStart(final FreeStyleProject proj, final SCMTrigger scmTrigger) {
        long delayStartPolling = 1000 * 60 * 5;//5 minute
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                scmTrigger.start(proj, false);
            }
        }, delayStartPolling);
    }

    private static void addParameter(FreeStyleProject proj, String parameterName, String parameterValue, boolean constantValue, String desc) throws IOException {
        ParametersDefinitionProperty parameters = proj.getProperty(ParametersDefinitionProperty.class);
        if (parameters == null) {
            parameters = new ParametersDefinitionProperty(new ArrayList<ParameterDefinition>());
            proj.addProperty(parameters);
        }

        if (constantValue) {
            if (parameters.getParameterDefinition(parameterName) == null) {
                //String name, List<String> choices, String defaultValue, String description
                ParameterDefinition param = new ChoiceParameterDefinition(parameterName, new String[]{parameterValue}, desc);
                parameters.getParameterDefinitions().add(param);
            }
        } else {
            if (parameters.getParameterDefinition(parameterName) == null) {
                //String name, List<String> choices, String defaultValue, String description
                ParameterDefinition param = new StringParameterDefinition(parameterName, null, desc);
                parameters.getParameterDefinitions().add(param);
            }
        }

    }

    public static void deleteExecutor(String id) {
        String jobName = buildDiscoveryJobName(TestingToolType.UFT, id);
        FreeStyleProject proj = (FreeStyleProject) Jenkins.getInstance().getItem(jobName);
        if (proj != null) {
            boolean waitBeforeDelete = false;

            if (proj.isBuilding()) {
                proj.getLastBuild().getExecutor().interrupt();
                waitBeforeDelete = true;
            } else if (proj.isInQueue()) {
                Jenkins.getInstance().getQueue().cancel(proj);
                waitBeforeDelete = true;
            }

            if (waitBeforeDelete) {
                try {
                    //we cancelled building/queue - wait before deleting the job
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }

            try {
                proj.delete();
            } catch (IOException | InterruptedException e) {
                logger.error("Failed to delete job  " + proj.getName() + " : " + e.getMessage());
            }
        }
    }

    /*public static void deleteJobWithParameter(String partOfName, String parameterName, String parameterValue) {
        List<FreeStyleProject> jobs = Jenkins.getInstance().getAllItems(FreeStyleProject.class);

        for (FreeStyleProject job : jobs) {
            if (job.getName().contains(partOfName)) {
                ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
                if (parameters != null) {
                    ParameterDefinition pd = parameters.getParameterDefinition(parameterName);
                    if (pd != null && pd.getDefaultParameterValue() != null) {
                        Object defaultValue = pd.getDefaultParameterValue().getValue();
                        if (parameterValue.equals(defaultValue)) {
                            try {
                                job.disable();
                                if (job.isBuilding() && job.getLastBuild().isBuilding()) {
                                    job.getLastBuild().getExecutor().interrupt();
                                } else if (job.isInQueue()) {
                                    Jenkins.getInstance().getQueue().cancel(job);
                                }

                            } catch (IOException e) {
                                logger.error("Failed to delete job  " + job.getName() + " : " + e.getMessage());
                            }
                            try {
                                job.delete();
                            } catch (IOException | InterruptedException e) {
                                logger.error("Failed to delete job  " + job.getName() + " : " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }*/

    private static void addAssignedNode(FreeStyleProject proj) {
        Computer[] computers = Jenkins.getInstance().getComputers();
        Set<String> labels = new HashSet();

        //add existing
        String assigned = proj.getAssignedLabelString();
        if (assigned != null) {
            String[] assignedArr = StringUtils.split(assigned, "||");
            for (String item : assignedArr) {
                labels.add(item.trim());
            }
        }

        //try to add new
        try {
            for (Computer computer : computers) {
                if (computer instanceof Jenkins.MasterComputer) {
                    continue;
                }

                String label = "" + computer.getNode().getSelfLabel();
                if (label.toLowerCase().contains("uft")) {
                    labels.add(label.trim());
                }
            }

            if (!labels.isEmpty()) {
                Label joinedLabel = Label.parseExpression(StringUtils.join(labels, "||"));
                proj.setAssignedLabel(joinedLabel);
            }

        } catch (IOException | ANTLRException e) {
            logger.error("Failed to  set addAssignedNode : " + e.getMessage());
        }
    }
}
