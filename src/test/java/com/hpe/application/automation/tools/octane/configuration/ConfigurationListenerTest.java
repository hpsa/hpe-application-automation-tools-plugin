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

package com.hpe.application.automation.tools.octane.configuration;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hpe.application.automation.tools.octane.tests.ExtensionUtil;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationListenerTest {

	@ClassRule
	static final public JenkinsRule rule = new JenkinsRule();

	private TestConfigurationListener listener;

	@Before
	public void init() throws Exception {
		HtmlPage configPage = rule.createWebClient().goTo("configure");
		HtmlForm form = configPage.getFormByName("config");

		form.getInputByName("_.uiLocation").setValueAttribute("http://localhost:8028/ui/?p=1001/1002");
		form.getInputByName("_.username").setValueAttribute("username");
		form.getInputByName("_.password").setValueAttribute("password");
		rule.submit(form);

		listener = ExtensionUtil.getInstance(rule, TestConfigurationListener.class);
		Assert.assertEquals("Listener count doesn't match 1",1, listener.getCount());

		List<ServerConfiguration> confs = listener.getConfigurationsChange();
		Assert.assertNotNull("Configuration is null",confs);
		Assert.assertEquals("Config size doesn't match 2",2, confs.size());
		Assert.assertEquals("location doesn't match localhost:8020","http://localhost:8028", confs.get(0).location);
		Assert.assertEquals("username doesn't match username","username", confs.get(0).username);
		Assert.assertNull(confs.get(1).location);
		Assert.assertNull(confs.get(1).username);
	}

	@Test
	public void testConfigurationListener() throws Exception {
		HtmlPage configPage = rule.createWebClient().goTo("configure");
		HtmlForm form = configPage.getFormByName("config");
		// password is cleared upon form retrieval (restore the value)
		form.getInputByName("_.password").setValueAttribute("password");

		// not increased on re-submit
		rule.submit(form);
		Assert.assertEquals("Listener count doesn't match 1",1, listener.getCount());

		configPage = rule.createWebClient().goTo("configure");
		form = configPage.getFormByName("config");
		// increased when configuration changes
		form.getInputByName("_.username").setValueAttribute("username2");
		rule.submit(form);
		Assert.assertEquals("Listener count doesn't match 2",2, listener.getCount());

		List<ServerConfiguration> confs = listener.getConfigurationsChange();
		Assert.assertNotNull("Configuration is null",confs);
		Assert.assertEquals("Config count doesn't match 2",2, confs.size());
		Assert.assertEquals("http://localhost:8028", confs.get(0).location);
		Assert.assertEquals("username2", confs.get(0).username);
		Assert.assertEquals("http://localhost:8028", confs.get(1).location);
		Assert.assertEquals("username", confs.get(1).username);
	}

	@TestExtension
	public static class TestConfigurationListener implements ConfigurationListener {

		private int count;
		private List<ServerConfiguration> newAndOld;

		@Override
		public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
			++count;
			newAndOld = new ArrayList<>();
			newAndOld.add(conf);
			newAndOld.add(oldConf);
		}

		public int getCount() {
			return count;
		}

		public List<ServerConfiguration> getConfigurationsChange() {
			return newAndOld;
		}
	}
}
