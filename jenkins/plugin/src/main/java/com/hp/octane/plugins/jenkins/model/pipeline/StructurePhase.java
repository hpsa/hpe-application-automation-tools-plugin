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
public final class StructurePhase extends AbstractPhase {
	private StructureItem[] items;

	public StructurePhase(String name, boolean blocking, List<AbstractProject> items) {
		super(name, blocking);
		this.items = new StructureItem[items.size()];
		for (int i = 0; i < this.items.length; i++) {
			this.items[i] = new StructureItem(items.get(i));
		}
	}

	@Exported(inline = true)
	public AbstractItem[] getJobs() {
		return items;
	}
}