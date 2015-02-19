package com.hp.octane.plugins.jenkins.model.snapshots;

import com.hp.octane.plugins.jenkins.model.api.AbstractItem;
import com.hp.octane.plugins.jenkins.model.api.AbstractPhase;
import com.hp.octane.plugins.jenkins.model.pipelines.StructureItem;
import com.hp.octane.plugins.jenkins.model.pipelines.StructurePhase;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
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
public final class SnapshotPhase extends AbstractPhase<SnapshotItem> {
	public SnapshotPhase(StructurePhase structurePhase, HashMap<String, ArrayList<AbstractBuild>> invokedBuilds) {
		super(structurePhase.getName(), structurePhase.getBlocking());
		ArrayList<AbstractBuild> tmpBuilds;
		StructureItem[] structures = structurePhase.getItems();
		SnapshotItem[] tmp = new SnapshotItem[structures.length];
		for (int i = 0; i < tmp.length; i++) {
			tmpBuilds = invokedBuilds == null ? null : invokedBuilds.get(structures[i].getName());
			if (tmpBuilds == null || tmpBuilds.size() == 0) {
				tmp[i] = new SnapshotItem((AbstractProject) Jenkins.getInstance().getItem(structures[i].getName()));
			} else {
				tmp[i] = new SnapshotItem(tmpBuilds.get(0));
				tmpBuilds.remove(0);
			}
		}
		super.setItems(tmp);
	}

	@Exported(inline = true, name = "builds")
	public SnapshotItem[] getItems() {
		return super.getItems();
	}
}