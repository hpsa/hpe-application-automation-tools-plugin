package com.hp.application.automation.tools.octane.executor;

import antlr.ANTLRException;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import com.hp.application.automation.tools.octane.actions.UFTTestDetectionPublisher;
import com.hp.application.automation.tools.octane.actions.UFTTestDetectionService;
import com.hp.application.automation.tools.results.RunResultRecorder;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import com.hp.octane.integrations.dto.executor.DiscoveryInfo;
import com.hp.octane.integrations.dto.executor.TestExecutionInfo;
import com.hp.octane.integrations.dto.executor.TestSuiteExecutionInfo;
import com.hp.octane.integrations.dto.scm.SCMRepository;
import com.hp.octane.integrations.dto.scm.SCMType;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.plugins.git.GitSCM;
import hudson.tasks.LogRotator;
import hudson.triggers.SCMTrigger;
import jenkins.model.BuildDiscarder;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This service is responsible to create jobs (discovery and execution) for execution process
 * Created by berkovir on 20/03/2017.
 */
public class TestExecutionJobCreatorService {


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
        proj.scheduleBuild2(0);
    }

    private static FreeStyleProject getExecutionJob(TestSuiteExecutionInfo suiteExecutionInfo) {

        String projectName = String.format("%s test execution job - executor id %s suite id %s",
                suiteExecutionInfo.getTestingToolType().toString(),
                suiteExecutionInfo.getExecutorId(),
                suiteExecutionInfo.getSuiteId());

        //validate creation of job
        FreeStyleProject proj = (FreeStyleProject) Jenkins.getInstance().getItem(projectName);
        if (proj == null) {
            try {
                proj = Jenkins.getInstance().createProject(FreeStyleProject.class, projectName);
                proj.setDescription(String.format("This job was created by HP AA Plugin for execution of %s tests, as part of Octane executor with id %s and suite with id %s",
                        suiteExecutionInfo.getTestingToolType().toString(), suiteExecutionInfo.getExecutorId(), suiteExecutionInfo.getSuiteId()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create FreeStyleProject : " + e.getMessage());
            }
        }

        setScmRepository(suiteExecutionInfo.getScmRepository(), proj);
        setBuildDiscarder(proj, 20);
        addParameter(proj, "executorId", suiteExecutionInfo.getExecutorId(), "Octane executor id");
        addParameter(proj, "suiteId", suiteExecutionInfo.getSuiteId(), "Octane suite id");

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
    }

    private static void setScmRepository(SCMRepository scmRepository, FreeStyleProject proj) {
        if (SCMType.GIT.equals(scmRepository.getType())) {
            try {
                GitSCM scm = new GitSCM(scmRepository.getUrl());
                proj.setScm(scm);
            } catch (IOException e) {
                throw new RuntimeException("Failed to set Git repository : " + e.getMessage());
            }
        } else {
            throw new RuntimeException("SCM repository " + scmRepository.getType() + " isn't supported yet");
        }
    }

    private static String prepareMtbxData(List<TestExecutionInfo> tests) {
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
            throw new RuntimeException("Failed to build MTBX content : " + e.getMessage());
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

        if (discoveryInfo.isForceFullDiscovery() && proj.getWorkspace() != null) {
            try {
                UFTTestDetectionService.removeInitialDetectionFlag(proj.getWorkspace());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to remove InitialDetectionFlag : " + e.getMessage());
            }
        }

        proj.scheduleBuild2(0);
    }

    private static FreeStyleProject getDiscoveryJob(DiscoveryInfo discoveryInfo) {

        String discoveryJobName = String.format("%s test discovery job - executor id %s", discoveryInfo.getTestingToolType().toString(), discoveryInfo.getExecutorId());
        //validate creation of job
        FreeStyleProject proj = (FreeStyleProject) Jenkins.getInstance().getItem(discoveryJobName);
        if (proj == null) {
            try {
                proj = Jenkins.getInstance().createProject(FreeStyleProject.class, discoveryJobName);
                proj.setDescription(String.format("This job was created by HP AA Plugin for discovery of %s tests, as part of Octane executor with id %s",
                        discoveryInfo.getTestingToolType().toString(), discoveryInfo.getExecutorId()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create FreeStyleProject : " + e.getMessage());
            }
        }

        setScmRepository(discoveryInfo.getScmRepository(), proj);
        setBuildDiscarder(proj, 20);
        addParameter(proj, "executorId", discoveryInfo.getExecutorId(), "Octane executor id");

        //set polling once in two minutes
        try {
            SCMTrigger scmTrigger = new SCMTrigger("H/2 * * * *");//H/2 * * * * : once in two minutes
            proj.addTrigger(scmTrigger);

            delayPollingStart(proj, scmTrigger);

        } catch (ANTLRException e) {
            throw new RuntimeException("Failed to set SCM Polling spec : " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to add  SCMTrigger : " + e.getMessage());
        }

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
    }

    private static void setBuildDiscarder(FreeStyleProject proj, int numBuildsToKeep) {
        try {
            int IRRELEVANT = -1;
            BuildDiscarder bd = new LogRotator(IRRELEVANT, numBuildsToKeep, IRRELEVANT, IRRELEVANT);
            proj.setBuildDiscarder(bd);
        } catch (IOException e) {
            throw new RuntimeException("Failed to set  build discarder : " + e.getMessage());
        }
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

    private static void addParameter(FreeStyleProject proj, String parameterName, String parameterValue, String desc) {

        ParametersDefinitionProperty parameters = proj.getProperty(ParametersDefinitionProperty.class);
        if (parameters == null) {
            try {
                parameters = new ParametersDefinitionProperty(new ArrayList<ParameterDefinition>());
                proj.addProperty(parameters);
            } catch (IOException e) {
                throw new RuntimeException("Failed to set  parameters : " + e.getMessage());
            }
        }

        if (parameters.getParameterDefinition(parameterName) == null) {
            //String name, List<String> choices, String defaultValue, String description
            ParameterDefinition param = new ChoiceParameterDefinition(parameterName, new String[]{parameterValue}, desc);
            parameters.getParameterDefinitions().add(param);
        }


    }

}
