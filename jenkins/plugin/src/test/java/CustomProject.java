import hudson.Extension;
import hudson.model.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/01/15
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */

@Extension
final public class CustomProject extends Project<FreeStyleProject, FreeStyleBuild> implements TopLevelItem {

	@Extension(ordinal = 1000)
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends AbstractProject.AbstractProjectDescriptor {
		public String getDisplayName() {
			return "Custom Project";
		}

		public CustomProject newInstance(ItemGroup itemGroup, String name) {
			return new CustomProject(itemGroup, name);
		}
	}

	public CustomProject() {
		super(null, null);
	}

	public CustomProject(ItemGroup group, String name) {
		super(group, name);
	}

	@Override
	public TopLevelItemDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	protected Class<FreeStyleBuild> getBuildClass() {
		return FreeStyleBuild.class;
	}
}