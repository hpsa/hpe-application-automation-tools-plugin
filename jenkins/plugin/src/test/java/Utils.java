import hudson.model.AbstractProject;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 13:55
 * To change this template use File | Settings | File Templates.
 */

public class Utils {
	static void buildProject(JenkinsRule.WebClient client, AbstractProject project) throws IOException, SAXException {
		client.goTo("job/" + project.getName() + "/build", "");
	}

	static void buildProjectWithParams(JenkinsRule.WebClient client, AbstractProject project, String params) throws IOException, SAXException {
		client.goTo("job/" + project.getName() + "/buildWithParameters?" + params, "");
	}
}
