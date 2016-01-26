package com.hp.octane.plugins.jenkins.model.processors.builders;

import com.hp.nga.integrations.dto.pipelines.StructurePhase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/01/15
 * Time: 00:59
 * To change this template use File | Settings | File Templates.
 */

public abstract class AbstractBuilderProcessor {
	protected ArrayList<StructurePhase> phases = new ArrayList<StructurePhase>();

	public List<StructurePhase> getPhases() {
		return phases;
	}
}
