package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.plugins.jenkins.actions.ProjectActions;
import hudson.model.Action;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */

public class ProjectActionsGeneralTest {
	final private String projectName = "root-job";

	@Rule
	final public JenkinsRule rule = new JenkinsRule();

	@Test
	public void testOctaneActionAdded() throws IOException {
		boolean actionFound = false;
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		List<Action> actions = p.getActions();
		for (Action action : actions)
			if (action.getClass() == ProjectActions.OctaneProjectActions.class) {
				actionFound = true;
				break;
			}
		assertTrue(actionFound);
	}

	@Test
	public void testOctaneActionsClass() throws IOException {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		ProjectActions.OctaneProjectActions octaneActions = new ProjectActions.OctaneProjectActions(p);
		assertEquals(octaneActions.getIconFileName(), null);
		assertEquals(octaneActions.getDisplayName(), null);
		assertEquals(octaneActions.getUrlName(), "octane");
	}
}