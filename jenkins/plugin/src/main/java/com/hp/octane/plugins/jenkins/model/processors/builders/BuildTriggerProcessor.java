package com.hp.octane.plugins.jenkins.model.processors.builders;

import com.hp.octane.plugins.jenkins.model.ModelFactory;
import hudson.model.AbstractProject;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Publisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */

public class BuildTriggerProcessor extends AbstractBuilderProcessor {
	private static final Logger logger = LogManager.getLogger(BuildTriggerProcessor.class);

	public BuildTriggerProcessor(Publisher publisher, AbstractProject project) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<>();
		List<AbstractProject> items = t.getChildProjects(project.getParent());
		for (Iterator<AbstractProject> iterator = items.iterator(); iterator.hasNext(); ) {
			AbstractProject next = iterator.next();
			if (next == null) {
				iterator.remove();
				logger.warn("encountered null project reference; considering it as corrupted configuration and skipping");
			}
		}
		super.phases.add(ModelFactory.createStructurePhase("downstream", false, items));
	}
}
