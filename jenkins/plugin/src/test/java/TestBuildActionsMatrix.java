import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */

public class TestBuildActionsMatrix {
	final private String projectName = "root-job";

	@Rule
	final public JenkinsRule rule = new JenkinsRule();
}
