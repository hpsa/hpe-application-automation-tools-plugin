package com.hp.octane.plugins.jenkins.build;

import hudson.Extension;
import hudson.model.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/08/14
 * Time: 10:45
 * To change this template use File | Settings | File Templates.
 */
@Extension
public class BuildActionsFactory extends TransientBuildActionFactory {
	@Override
	public Collection<? extends Action> createFor(Run run) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new BuildActions(run));
		return actions;
	}
}