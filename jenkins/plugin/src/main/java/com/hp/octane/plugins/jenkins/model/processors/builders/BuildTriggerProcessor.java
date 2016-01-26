package com.hp.octane.plugins.jenkins.model.processors.builders;

import com.hp.nga.integrations.dto.pipelines.StructurePhase;
import com.hp.octane.plugins.jenkins.model.pipelines.PipelinesFactory;
import hudson.model.AbstractProject;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Publisher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */

public class BuildTriggerProcessor extends AbstractBuilderProcessor {
	private static final Logger logger = Logger.getLogger(BuildTriggerProcessor.class.getName());

	public BuildTriggerProcessor(Publisher publisher, AbstractProject project) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<StructurePhase>();
		List<AbstractProject> items = t.getChildProjects(project.getParent());
		for (Iterator<AbstractProject> iterator = items.iterator(); iterator.hasNext();) {
			AbstractProject next = iterator.next();
			if (next == null) {
				iterator.remove();
				logger.severe("encountered null project reference; considering it as corrupted configuration and skipping");
			}
		}
//		super.phases.add(new StructurePhase("downstream", false, items));
		super.phases.add(PipelinesFactory.createStructurePhase("downstream", false, items));

	}
}
