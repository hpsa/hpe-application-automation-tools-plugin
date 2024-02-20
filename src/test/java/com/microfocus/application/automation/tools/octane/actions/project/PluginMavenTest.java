/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.actions.project;

import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.pipelines.PipelinePhase;
import com.microfocus.application.automation.tools.octane.OctanePluginTestBase;
import com.microfocus.application.automation.tools.octane.tests.TestUtils;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.*;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Fingerprinter;
import hudson.tasks.Shell;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */

public class PluginMavenTest extends OctanePluginTestBase {
    //  Structure test: maven, no params, no children
    @Test
    public void testStructureMavenNoParamsNoChildren() throws IOException {
        String projectName = "root-job-" + UUID.randomUUID().toString();
        MavenModuleSet project = rule.createProject(MavenModuleSet.class, projectName);
        project.runHeadless();

        String taskUrl = "nga/api/v1/jobs/" + projectName;
        PipelineNode pipeline = TestUtils.sendTask(taskUrl, PipelineNode.class);

        assertEquals(projectName, pipeline.getJobCiId());
        assertEquals(projectName, pipeline.getName());
        assertEquals(0, pipeline.getParameters().size());
        assertEquals(0, pipeline.getPhasesInternal().size());
        assertEquals(0, pipeline.getPhasesPostBuild().size());
    }

    @Test
    public void testDoRun() throws IOException, InterruptedException {
        String projectName = "root-job-" + UUID.randomUUID().toString();
        int retries = 0;
        MavenModuleSet p = rule.createProject(MavenModuleSet.class, projectName);
        p.runHeadless();

        String taskUrl = "nga/api/v1/jobs/" + projectName + "/run";
        TestUtils.sendTask(taskUrl, null);

        while ((p.getLastBuild() == null || p.getLastBuild().isBuilding()) && ++retries < 20) {
            Thread.sleep(1000);
        }
        assertEquals(p.getBuilds().toArray().length, 1);
    }

    @Test
    public void testStructureMavenWithParamsNoChildren() throws IOException, SAXException {
        String projectName = "root-job-" + UUID.randomUUID().toString();
        MavenModuleSet p = rule.createProject(MavenModuleSet.class, projectName);
        p.runHeadless();
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
                (ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
                (ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string"),
                (ParameterDefinition) new TextParameterDefinition("ParamC", "txt", "text"),
                (ParameterDefinition) new ChoiceParameterDefinition("ParamD", new String[]{"one", "two", "three"}, "choice"),
                (ParameterDefinition) new FileParameterDefinition("ParamE", "file param")
        ));
        p.addProperty(params);


        CIParameter tmpParam;
        String taskUrl = "nga/api/v1/jobs/" + projectName;
        PipelineNode pipeline = TestUtils.sendTask(taskUrl, PipelineNode.class);

        assertEquals(projectName, pipeline.getJobCiId());
        assertEquals(projectName, pipeline.getName());
        assertEquals(5, pipeline.getParameters().size());
        assertEquals(0, pipeline.getPhasesInternal().size());
        assertEquals(0, pipeline.getPhasesPostBuild().size());

        tmpParam = pipeline.getParameters().get(0);
        assertEquals("ParamA", tmpParam.getName());
        assertEquals(CIParameterType.BOOLEAN, tmpParam.getType());
        assertEquals("bool", tmpParam.getDescription());
        assertEquals(true, tmpParam.getDefaultValue());
        assertNull(tmpParam.getChoices());

        tmpParam = pipeline.getParameters().get(1);
        assertEquals("ParamB", tmpParam.getName());
        assertEquals(CIParameterType.STRING, tmpParam.getType());
        assertEquals("string", tmpParam.getDescription());
        assertEquals("str", tmpParam.getDefaultValue());
        assertNull(tmpParam.getChoices());

        tmpParam = pipeline.getParameters().get(2);
        assertEquals("ParamC", tmpParam.getName());
        assertEquals(CIParameterType.STRING, tmpParam.getType());
        assertEquals("text", tmpParam.getDescription());
        assertEquals("txt", tmpParam.getDefaultValue());
        assertNull(tmpParam.getChoices());

        tmpParam = pipeline.getParameters().get(3);
        assertEquals("ParamD", tmpParam.getName());
        assertEquals(CIParameterType.STRING, tmpParam.getType());
        assertEquals("choice", tmpParam.getDescription());
        assertEquals("one", tmpParam.getDefaultValue());
        assertNotNull(tmpParam.getChoices());
        assertEquals(3, tmpParam.getChoices().length);
        assertEquals("one", tmpParam.getChoices()[0]);
        assertEquals("two", tmpParam.getChoices()[1]);
        assertEquals("three", tmpParam.getChoices()[2]);

        tmpParam = pipeline.getParameters().get(4);
        assertEquals("ParamE", tmpParam.getName());
        assertEquals(CIParameterType.FILE, tmpParam.getType());
        assertEquals("file param", tmpParam.getDescription());
        assertEquals("", tmpParam.getDefaultValue());
        assertNull(tmpParam.getChoices());
    }

    //  Structure test: maven, with params, with children
    //
    @Test
    public void testStructureMavenWithParamsWithChildren() throws IOException, SAXException {
        String projectName = "root-job-" + UUID.randomUUID().toString();
        MavenModuleSet p = rule.createProject(MavenModuleSet.class, projectName);
        p.runHeadless();
        FreeStyleProject p1 = rule.createFreeStyleProject("jobA");
        MatrixProject p2 = rule.createProject(MatrixProject.class, "jobB");
        FreeStyleProject p3 = rule.createFreeStyleProject("jobC");
        MatrixProject p4 = rule.createProject(MatrixProject.class, "jobD");
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
                (ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
                (ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string")
        ));
        p.addProperty(params);
        p.getPrebuilders().add(new TriggerBuilder(Arrays.asList(
                new BlockableBuildTriggerConfig("jobA, jobB", new BlockingBehaviour(
                        Result.FAILURE,
                        Result.UNSTABLE,
                        Result.FAILURE
                ), Arrays.asList(new AbstractBuildParameters[0])),
                new BlockableBuildTriggerConfig("jobC, jobD", null, Arrays.asList(new AbstractBuildParameters[0]))
        )));
        p.getPrebuilders().add(new Shell(""));
        p.getPostbuilders().add(new Shell(""));
        p.getPostbuilders().add(new TriggerBuilder(Arrays.asList(
                new BlockableBuildTriggerConfig("jobA, jobB", new BlockingBehaviour(
                        Result.FAILURE,
                        Result.UNSTABLE,
                        Result.FAILURE
                ), Arrays.asList(new AbstractBuildParameters[0])),
                new BlockableBuildTriggerConfig("jobC, jobD", null, Arrays.asList(new AbstractBuildParameters[0]))
        )));
        p.getPublishersList().add(new BuildTrigger("jobA, jobB", Result.SUCCESS));
        p.getPublishersList().add(new Fingerprinter(""));
        p.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(Arrays.asList(
                new BuildTriggerConfig("jobC, jobD", ResultCondition.ALWAYS, false, null)
        )));

        List<PipelinePhase> tmpPhases;
        PipelineNode tmpNode;
        CIParameter tmpParam;

        String taskUrl = "nga/api/v1/jobs/" + projectName;
        PipelineNode pipeline = TestUtils.sendTask(taskUrl, PipelineNode.class);

        assertEquals(projectName, pipeline.getJobCiId());
        assertEquals(projectName, pipeline.getName());
        assertEquals(2, pipeline.getParameters().size());

        tmpParam = pipeline.getParameters().get(0);
        assertEquals("ParamA", tmpParam.getName());
        assertEquals(CIParameterType.BOOLEAN, tmpParam.getType());
        assertEquals("bool", tmpParam.getDescription());
        assertEquals(true, tmpParam.getDefaultValue());
        assertNull(tmpParam.getChoices());

        tmpParam = pipeline.getParameters().get(1);
        assertEquals("ParamB", tmpParam.getName());
        assertEquals(CIParameterType.STRING, tmpParam.getType());
        assertEquals("string", tmpParam.getDescription());
        assertEquals("str", tmpParam.getDefaultValue());
        assertNull(tmpParam.getChoices());

        //  Phases Internal
        //
        tmpPhases = pipeline.getPhasesInternal();
        assertEquals(4, tmpPhases.size());

        //  Phase 0
        assertEquals("pre-maven", tmpPhases.get(0).getName());
        assertEquals(true, tmpPhases.get(0).isBlocking());
        assertEquals(2, tmpPhases.get(0).getJobs().size());

        tmpNode = tmpPhases.get(0).getJobs().get(0);
        assertEquals("jobA", tmpNode.getJobCiId());
        assertEquals("jobA", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());
        tmpNode = tmpPhases.get(0).getJobs().get(1);
        assertEquals("jobB", tmpNode.getJobCiId());
        assertEquals("jobB", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());

        //  Phase 1
        assertEquals("pre-maven", tmpPhases.get(1).getName());
        assertEquals(false, tmpPhases.get(1).isBlocking());
        assertEquals(2, tmpPhases.get(1).getJobs().size());

        tmpNode = tmpPhases.get(1).getJobs().get(0);
        assertEquals("jobC", tmpNode.getJobCiId());
        assertEquals("jobC", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());
        tmpNode = tmpPhases.get(1).getJobs().get(1);
        assertEquals("jobD", tmpNode.getJobCiId());
        assertEquals("jobD", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());

        //  Phase 2
        assertEquals("post-maven", tmpPhases.get(2).getName());
        assertEquals(true, tmpPhases.get(2).isBlocking());
        assertEquals(2, tmpPhases.get(2).getJobs().size());

        tmpNode = tmpPhases.get(2).getJobs().get(0);
        assertEquals("jobA", tmpNode.getJobCiId());
        assertEquals("jobA", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());
        tmpNode = tmpPhases.get(2).getJobs().get(1);
        assertEquals("jobB", tmpNode.getJobCiId());
        assertEquals("jobB", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());

        //  Phase 3
        assertEquals("post-maven", tmpPhases.get(3).getName());
        assertEquals(false, tmpPhases.get(3).isBlocking());
        assertEquals(2, tmpPhases.get(3).getJobs().size());

        tmpNode = tmpPhases.get(3).getJobs().get(0);
        assertEquals("jobC", tmpNode.getJobCiId());
        assertEquals("jobC", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());
        tmpNode = tmpPhases.get(3).getJobs().get(1);
        assertEquals("jobD", tmpNode.getJobCiId());
        assertEquals("jobD", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());

        //  Phases Post build
        //
        tmpPhases = pipeline.getPhasesPostBuild();
        assertEquals(2, tmpPhases.size());

        //  Phase 0
        assertEquals("downstream", tmpPhases.get(0).getName());
        assertEquals(false, tmpPhases.get(0).isBlocking());
        assertEquals(2, tmpPhases.get(0).getJobs().size());

        tmpNode = tmpPhases.get(0).getJobs().get(0);
        assertEquals("jobA", tmpNode.getJobCiId());
        assertEquals("jobA", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());
        tmpNode = tmpPhases.get(0).getJobs().get(1);
        assertEquals("jobB", tmpNode.getJobCiId());
        assertEquals("jobB", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());

        //  Phase 1
        assertEquals("", tmpPhases.get(1).getName());
        assertEquals(false, tmpPhases.get(1).isBlocking());
        assertEquals(2, tmpPhases.get(1).getJobs().size());

        tmpNode = tmpPhases.get(1).getJobs().get(0);
        assertEquals("jobC", tmpNode.getJobCiId());
        assertEquals("jobC", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());
        tmpNode = tmpPhases.get(1).getJobs().get(1);
        assertEquals("jobD", tmpNode.getJobCiId());
        assertEquals("jobD", tmpNode.getName());
        assertEquals(0, tmpNode.getParameters().size());
        assertEquals(0, tmpNode.getPhasesInternal().size());
        assertEquals(0, tmpNode.getPhasesPostBuild().size());
    }
}
