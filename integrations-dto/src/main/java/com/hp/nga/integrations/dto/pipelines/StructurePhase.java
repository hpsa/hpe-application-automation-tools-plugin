package com.hp.nga.integrations.dto.pipelines;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

public final class StructurePhase extends AbstractPhase<StructureItem> {
	private static final Logger logger = Logger.getLogger(StructurePhase.class.getName());

	public StructurePhase(String name, boolean blocking, List<AbstractProject> items) {
		super(name, blocking);
		StructureItem[] tmp = new StructureItem[items.size()];
		for (int i = 0; i < tmp.length; i++) {
			if (items.get(i) != null) {
				tmp[i] = new StructureItem(items.get(i));
			} else {
				logger.warning("One of referenced jobs is null, your Jenkins config probably broken, skipping this job...");
			}
		}
		super.setItems(tmp);
	}

	public StructureItem[] getJobs() {
		return super.getItems();
	}
}