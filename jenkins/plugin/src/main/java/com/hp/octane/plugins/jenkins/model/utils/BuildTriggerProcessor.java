package com.hp.octane.plugins.jenkins.model.utils;

import com.hp.octane.plugins.jenkins.model.pipelines.StructurePhase;
import hudson.model.AbstractProject;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Publisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */

public class BuildTriggerProcessor extends AbstractBuilderProcessor {
	public BuildTriggerProcessor(Publisher publisher, AbstractProject project) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<StructurePhase>();
		List<AbstractProject> items = t.getChildProjects(project.getParent());
		super.phases.add(new StructurePhase("downstream", false, items));
	}
}
