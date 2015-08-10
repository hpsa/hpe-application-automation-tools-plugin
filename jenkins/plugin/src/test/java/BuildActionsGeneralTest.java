import com.hp.octane.plugins.jenkins.actions.BuildActions;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 09:08
 * To change this template use File | Settings | File Templates.
 */

public class BuildActionsGeneralTest {
	final private String projectName = "root-job";

	@Rule
	final public JenkinsRule rule = new JenkinsRule();

	@Test
	public void testOctaneActionAdded() throws Exception {
		List<BuildActions> factoryList = rule.getInstance().getExtensionList(BuildActions.class);
		assertEquals(factoryList.size(), 1);
		assertEquals(factoryList.get(0).type(), AbstractBuild.class);

		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		AbstractBuild b = rule.buildAndAssertSuccess(p);
		List<BuildActions.OctaneBuildActions> actions = b.getActions(BuildActions.OctaneBuildActions.class);
		assertEquals(actions.size(), 1);
	}

	@Test
	public void testOctaneActionsClass() throws Exception {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		AbstractBuild b = rule.buildAndAssertSuccess(p);
		BuildActions.OctaneBuildActions octaneActions = new BuildActions.OctaneBuildActions(b);
		assertEquals(octaneActions.getIconFileName(), null);
		assertEquals(octaneActions.getDisplayName(), null);
		assertEquals(octaneActions.getUrlName(), "octane");
	}


}
