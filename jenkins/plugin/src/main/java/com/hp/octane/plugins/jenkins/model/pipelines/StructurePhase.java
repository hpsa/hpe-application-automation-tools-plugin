package com.hp.octane.plugins.jenkins.model.pipelines;

import com.hp.octane.plugins.jenkins.model.api.AbstractPhase;
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
public final class StructurePhase extends AbstractPhase<StructureItem> {
	public StructurePhase(String name, boolean blocking, List<AbstractProject> items) {
		super(name, blocking);
		StructureItem[] tmp = new StructureItem[items.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = new StructureItem(items.get(i));
		}
		super.setItems(tmp);
	}

	@Exported(inline = true, name = "jobs")
	public StructureItem[] getItems() {
		return super.getItems();
	}
}