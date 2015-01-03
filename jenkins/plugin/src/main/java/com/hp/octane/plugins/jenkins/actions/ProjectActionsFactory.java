package com.hp.octane.plugins.jenkins.actions;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/08/14
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */
@Extension
public class ProjectActionsFactory extends TransientProjectActionFactory {
	@Override
	public Collection<? extends Action> createFor(AbstractProject project) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new ProjectActions(project));
		return actions;
	}
}
