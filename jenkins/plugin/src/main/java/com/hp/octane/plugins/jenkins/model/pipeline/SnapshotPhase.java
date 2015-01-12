package com.hp.octane.plugins.jenkins.model.pipeline;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class SnapshotPhase extends AbstractPhase {
	private SnapshotItem[] items;

	public SnapshotPhase(StructurePhase structurePhase, HashMap<String, ArrayList<AbstractBuild>> invokedBuilds) {
		super(structurePhase.getName(), structurePhase.getBlocking());
		ArrayList<AbstractBuild> tmpBuilds;
		StructureItem[] structures = (StructureItem[]) structurePhase.getJobs();
		items = new SnapshotItem[structures.length];
		for (int i = 0; i < items.length; i++) {
			tmpBuilds = invokedBuilds == null ? null : invokedBuilds.get(structures[i].getName());
			if (tmpBuilds == null || tmpBuilds.size() == 0) {
				items[i] = new SnapshotItem((AbstractProject) Jenkins.getInstance().getItem(structures[i].getName()));
			} else {
				items[i] = new SnapshotItem(tmpBuilds.get(0));
				tmpBuilds.remove(0);
			}
		}
	}

	@Override
	AbstractItem[] provideItems() {
		return items;
	}
}