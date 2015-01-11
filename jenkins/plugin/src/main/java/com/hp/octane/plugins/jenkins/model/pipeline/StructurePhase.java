package com.hp.octane.plugins.jenkins.model.pipeline;

import hudson.model.AbstractProject;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class StructurePhase {
	private String name;
	private boolean blocking;
	private StructureItem[] items;

	public StructurePhase(String name, boolean blocking, List<AbstractProject> items) {
		this.name = name;
		this.blocking = blocking;
		this.items = new StructureItem[items.size()];
		for (int i = 0; i < this.items.length; i++) {
			this.items[i] = new StructureItem(items.get(i));
		}
	}

	@Exported(inline = true)
	public String getName() {
		return name;
	}

	@Exported(inline = true)
	public boolean getBlocking() {
		return blocking;
	}

	@Exported(inline = true)
	public StructureItem[] getJobs() {
		return items;
	}
}