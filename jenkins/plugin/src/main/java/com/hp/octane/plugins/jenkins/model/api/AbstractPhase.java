package com.hp.octane.plugins.jenkins.model.api;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/01/15
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public abstract class AbstractPhase {
	private String name;
	private boolean blocking;

	protected AbstractPhase(String name, boolean blocking) {
		this.name = name;
		this.blocking = blocking;
	}

	@Exported(inline = true)
	public String getName() {
		return name;
	}

	@Exported(inline = true)
	public boolean getBlocking() {
		return blocking;
	}
}
