/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.actions.plugin;

import com.gargoylesoftware.htmlunit.Page;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.octane.integrations.dto.general.CIServerTypes;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hpe.application.automation.tools.octane.actions.PluginActions;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import hudson.model.BooleanParameterDefinition;
import hudson.model.FileParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import jenkins.model.Jenkins;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 07/01/15
 * Time: 22:09
 * To change this template use File | Settings | File Templates.
 */

@SuppressWarnings({"squid:S2699","squid:S3658","squid:S2259","squid:S1872","squid:S2925","squid:S109","squid:S1607","squid:S2701","squid:S3578","squid:S2698"})
public class PluginActionsTest {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@ClassRule
	public static final JenkinsRule rule = new JenkinsRule();
	private final JenkinsRule.WebClient client = rule.createWebClient();

	@Test
	public void testPluginActionsMethods() {
		PluginActions pluginActions = new PluginActions();
		assertEquals(pluginActions.getIconFileName(), null);
		assertEquals(pluginActions.getDisplayName(), null);
		assertEquals("nga", pluginActions.getUrlName());
	}

	@Test
	public void testPluginActions_REST_Status() throws IOException, SAXException {
		Page page = client.goTo("nga/api/v1/status", "application/json");
		CIProviderSummaryInfo status = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), CIProviderSummaryInfo.class);

		assertNotNull(status);

		assertNotNull(status.getServer());
		assertEquals(CIServerTypes.JENKINS, status.getServer().getType());
		assertEquals(Jenkins.VERSION, status.getServer().getVersion());
		assertEquals(rule.getInstance().getRootUrl(), status.getServer().getUrl() + "/");
		assertEquals(ConfigurationService.getModel().getIdentity(), status.getServer().getInstanceId());
		assertEquals(ConfigurationService.getModel().getIdentityFrom(), status.getServer().getInstanceIdFrom());
		assertNotNull(status.getServer().getSendingTime());

		assertNotNull(status.getPlugin());
		assertEquals(ConfigurationService.getPluginVersion(), status.getPlugin().getVersion());
	}

	@Test
	public void testPluginActions_REST_Jobs_NoParams() throws IOException, SAXException {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		Page page = client.goTo("nga/api/v1/jobs", "application/json");
		CIJobsList response = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), CIJobsList.class);

		assertNotNull(response);
		assertNotNull(response.getJobs());
		assertEquals(rule.getInstance().getTopLevelItemNames().size(), response.getJobs().length);

		rule.createFreeStyleProject(projectName);
		page = client.goTo("nga/api/v1/jobs", "application/json");
		response = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), CIJobsList.class);

		assertNotNull(response);
		assertNotNull(response.getJobs());
		for (PipelineNode ciJob : response.getJobs()) {
			if (projectName.equals(ciJob.getName())) {
				assertNotNull(ciJob.getParameters());
				assertEquals(0, ciJob.getParameters().size());
			}
		}
	}

	@Test
	public void testPluginActions_REST_Jobs_WithParams() throws IOException, SAXException {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject fsp;
		Page page = client.goTo("nga/api/v1/jobs", "application/json");
		CIJobsList response = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), CIJobsList.class);

		assertNotNull(response);
		assertNotNull(response.getJobs());
		assertEquals(0, response.getJobs().length);

		fsp = rule.createFreeStyleProject(projectName);
		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string"),
				(ParameterDefinition) new FileParameterDefinition("ParamC", "file param")
		));
		fsp.addProperty(params);

		page = client.goTo("nga/api/v1/jobs", "application/json");
		response = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), CIJobsList.class);

		assertNotNull(response);
		assertNotNull(response.getJobs());
		assertEquals(1, response.getJobs().length);
		assertEquals(projectName, response.getJobs()[0].getName());
		assertNotNull(response.getJobs()[0].getParameters());
		assertEquals(3, response.getJobs()[0].getParameters().size());

		//  Test ParamA
		assertNotNull(response.getJobs()[0].getParameters().get(0));
		assertEquals("ParamA", response.getJobs()[0].getParameters().get(0).getName());
		assertEquals(CIParameterType.BOOLEAN, response.getJobs()[0].getParameters().get(0).getType());
		assertEquals("bool", response.getJobs()[0].getParameters().get(0).getDescription());
		assertEquals(true, response.getJobs()[0].getParameters().get(0).getDefaultValue());

		//  Test ParamB
		assertNotNull(response.getJobs()[0].getParameters().get(1));
		assertEquals("ParamB", response.getJobs()[0].getParameters().get(1).getName());
		assertEquals(CIParameterType.STRING, response.getJobs()[0].getParameters().get(1).getType());
		assertEquals("string", response.getJobs()[0].getParameters().get(1).getDescription());
		assertEquals("str", response.getJobs()[0].getParameters().get(1).getDefaultValue());

		//  Test ParamC
		assertNotNull(response.getJobs()[0].getParameters().get(2));
		assertEquals("ParamC", response.getJobs()[0].getParameters().get(2).getName());
		assertEquals(CIParameterType.FILE, response.getJobs()[0].getParameters().get(2).getType());
		assertEquals("file param", response.getJobs()[0].getParameters().get(2).getDescription());
		assertEquals("", response.getJobs()[0].getParameters().get(2).getDefaultValue());
	}
}
