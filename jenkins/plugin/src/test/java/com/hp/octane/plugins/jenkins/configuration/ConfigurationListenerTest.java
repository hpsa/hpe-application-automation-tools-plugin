// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hp.octane.plugins.jenkins.ExtensionUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationListenerTest {

	@Rule
	final public JenkinsRule rule = new JenkinsRule();

	private TestConfigurationListener listener;

	@Before
	public void init() throws Exception {
		HtmlPage configPage = rule.createWebClient().goTo("configure");
		HtmlForm form = configPage.getFormByName("config");

		form.getInputByName("_.location").setValueAttribute("http://localhost:8008");
		form.getInputByName("_.domain").setValueAttribute("domain");
		form.getInputByName("_.project").setValueAttribute("project");
		form.getInputByName("_.username").setValueAttribute("username");
		form.getInputByName("_.password").setValueAttribute("password");
		rule.submit(form);

		listener = ExtensionUtil.getInstance(rule, TestConfigurationListener.class);
		Assert.assertEquals(1, listener.getCount());

		List<ServerConfiguration> confs = listener.getConfigurationsChange();
		Assert.assertNotNull(confs);
		Assert.assertEquals(2, confs.size());
		Assert.assertEquals("http://localhost:8008", confs.get(0).location);
		Assert.assertEquals("project", confs.get(0).project);
		Assert.assertNull(confs.get(1).location);
		Assert.assertNull(confs.get(1).project);
	}

	@Test
	public void testConfigurationListener() throws Exception {
		HtmlPage configPage = rule.createWebClient().goTo("configure");
		HtmlForm form = configPage.getFormByName("config");
		// password is cleared upon form retrieval (restore the value)
		form.getInputByName("_.password").setValueAttribute("password");

		// not increased on re-submit
		rule.submit(form);
		Assert.assertEquals(1, listener.getCount());

		// increased when configuration changes
		form.getInputByName("_.project").setValueAttribute("project2");
		rule.submit(form);
		Assert.assertEquals(2, listener.getCount());

		List<ServerConfiguration> confs = listener.getConfigurationsChange();
		Assert.assertNotNull(confs);
		Assert.assertEquals(2, confs.size());
		Assert.assertEquals("http://localhost:8008", confs.get(0).location);
		Assert.assertEquals("project2", confs.get(0).project);
		Assert.assertEquals("http://localhost:8008", confs.get(1).location);
		Assert.assertEquals("project", confs.get(1).project);
	}

	@TestExtension
	public static class TestConfigurationListener implements ConfigurationListener {

		private int count;
		private List<ServerConfiguration> newAndOld;

		@Override
		public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
			++count;
			newAndOld = new ArrayList<ServerConfiguration>();
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
