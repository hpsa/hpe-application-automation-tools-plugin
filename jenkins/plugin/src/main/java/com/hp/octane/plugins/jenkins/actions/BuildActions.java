package com.hp.octane.plugins.jenkins.actions;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.tests.TestApi;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.model.TransientActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/08/14
 * Time: 10:45
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class BuildActions extends TransientActionFactory<AbstractBuild> {
	private static final Logger logger = LogManager.getLogger(BuildActions.class);

	private JenkinsMqmRestClientFactory clientFactory;

	static final public class OctaneBuildActions implements RunAction2 {
		AbstractBuild build;
		JenkinsMqmRestClientFactory clientFactory;

		public OctaneBuildActions(AbstractBuild b, JenkinsMqmRestClientFactory clientFactory) {
			build = b;
			this.clientFactory = clientFactory;
		}

		public void onAttached(Run<?, ?> run) {
		}

		public void onLoad(Run<?, ?> run) {
		}

		public String getIconFileName() {
			return null;
		}

		public String getDisplayName() {
			return null;
		}

		public String getUrlName() {
			return "nga";
		}

		public TestApi getTests() {
			return new TestApi(build, clientFactory);
		}
	}

	@Override
	public Class<AbstractBuild> type() {
		return AbstractBuild.class;
	}

	@Override
	@Nonnull
	public Collection<? extends Action> createFor(@Nonnull AbstractBuild build) {
		ArrayList<Action> actions = new ArrayList<>();
		actions.add(new OctaneBuildActions(build, clientFactory));
		return actions;
	}

	@Inject
	public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
		this.clientFactory = clientFactory;
	}

	/*
	 * To be used in tests only.
	 */
	public void _setMqmRestClientFactory(JenkinsMqmRestClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}
}