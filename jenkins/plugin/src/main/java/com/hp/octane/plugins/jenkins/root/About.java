package com.hp.octane.plugins.jenkins.root;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/08/14
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
@ExportedBean
public class About {
	String version;

	public About() {
		version = "1.2";
	}

	@Exported(inline = true)
	public String getVersion() {
		return version;
	}
}
