package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.model.parameters.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.snapshots.SnapshotResult;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.scm.SCMDataFactory;
import com.hp.octane.plugins.jenkins.model.events.CIEventFinished;
import com.hp.octane.plugins.jenkins.model.events.CIEventStarted;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/08/14
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
@Extension
public final class RunListenerImpl extends RunListener<Run> {

	@Override
	@SuppressWarnings("unchecked")
	public void onStarted(Run r, TaskListener listener) {
		if (r instanceof AbstractBuild) {
			AbstractBuild build = (AbstractBuild) r;
			CIEventStarted event = new CIEventStarted(
					build.getProject().getName(),
					build.getNumber(),
					build.getStartTimeInMillis(),
					build.getEstimatedDuration(),
					CIEventCausesFactory.convertCause(build.getCauses()),
					getParameters(build)
			);
			EventDispatcher.dispatchEvent(event);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onCompleted(Run r, @Nonnull TaskListener listener) {
		if (r instanceof AbstractBuild) {
			AbstractBuild build = (AbstractBuild) r;
			SnapshotResult result;
			if (build.getResult() == Result.SUCCESS) {
				result = SnapshotResult.SUCCESS;
			} else if (build.getResult() == Result.ABORTED) {
				result = SnapshotResult.ABORTED;
			} else if (build.getResult() == Result.FAILURE) {
				result = SnapshotResult.FAILURE;
			} else if (build.getResult() == Result.UNSTABLE) {
				result = SnapshotResult.UNSTABLE;
			} else {
				result = SnapshotResult.UNAVAILABLE;
			}
			CIEventFinished event = new CIEventFinished(
					build.getProject().getName(),
					build.getNumber(),
					result,
					build.getDuration(),
					SCMDataFactory.create(build),
					CIEventCausesFactory.convertCause(build.getCauses())
			);
			EventDispatcher.dispatchEvent(event);
		}
	}

	//  TODO: this code is found in AbstractItem/AbstractProjectProcessor - unite them
	private ParameterInstance[] getParameters(AbstractBuild build) {
		ParameterInstance[] parameters = null;

		AbstractProject project = build.getProject();
		List<ParameterDefinition> paramDefinitions;
		ParameterConfig[] parametersConfigs;

		ParametersAction parametersAction = build.getAction(ParametersAction.class);
		ArrayList<ParameterValue> parametersValues;

		if (project.isParameterized()) {
			paramDefinitions = ((ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions();
			parametersConfigs = new ParameterConfig[paramDefinitions.size()];
			for (int i = 0; i < parametersConfigs.length; i++) {
				parametersConfigs[i] = new ParameterConfig(paramDefinitions.get(i));
			}

			if (parametersAction != null) {
				parametersValues = new ArrayList<ParameterValue>(parametersAction.getParameters());
			} else {
				parametersValues = new ArrayList<ParameterValue>();
			}

			parameters = new ParameterInstance[parametersConfigs.length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = new ParameterInstance(parametersConfigs[i]);
				for (int j = 0; j < parametersValues.size(); j++) {
					//  TODO: reevaluate config to value mapping logic
					if (parametersValues.get(j).getName().compareTo(parametersConfigs[i].getName()) == 0) {
						parameters[i].setValue(parametersValues.get(j).getValue());
						parametersValues.remove(j);
						break;
					}
				}
			}
		}

		return parameters;
	}
}
