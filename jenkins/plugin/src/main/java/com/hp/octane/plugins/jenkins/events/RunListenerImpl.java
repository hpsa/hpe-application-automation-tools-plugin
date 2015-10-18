package com.hp.octane.plugins.jenkins.events;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.model.snapshots.SnapshotResult;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.events.CIEventFinished;
import com.hp.octane.plugins.jenkins.model.events.CIEventStarted;
import com.hp.octane.plugins.jenkins.tests.TestListener;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/08/14
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class RunListenerImpl extends RunListener<Run> {
	private static Logger logger = Logger.getLogger(RunListenerImpl.class.getName());

	@Inject
	private TestListener testListener;

	@Override
	@SuppressWarnings("unchecked")
	public void onStarted(Run r, TaskListener listener) {
    CIEventStarted event = null;
    if(r.getParent() instanceof MatrixConfiguration){
      AbstractBuild build = (AbstractBuild) r;
      logger.info("######################## OnStarted SubBuild "+build.getProject().getName() +
        ",Build Number:"+build.getNumber()+
        ", Parent Name:"+((MatrixRun) r).getParentBuild().getParent().getName()+" Parent Number:"+((MatrixRun) r).getParentBuild().getNumber());
      event = new CIEventStarted(
        ((MatrixRun) r).getParentBuild().getParent().getName(),
        ((MatrixRun) r).getParentBuild().getNumber(),
        build.getNumber(),
        build.getStartTimeInMillis(),
        build.getEstimatedDuration(),
        CIEventCausesFactory.processCauses(((MatrixRun) r).getParentBuild().getCauses()),
        ParameterProcessors.getInstances(build)
      );

      ParameterInstance[] parameters = event.getParameters();
      for(int i = 0 ; i < parameters.length; i++) {
        logger.info("%%%%%%%%%%%%%%% SubProject Parameter:"+parameters[i].getName() + ":"+parameters[i].getValue());
      }
    }else  if (r instanceof AbstractBuild) {
			AbstractBuild build = (AbstractBuild) r;
      logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> OnStarted Build "+build.getProject().getName());
			event = new CIEventStarted(
					build.getProject().getName(),
					build.getNumber(),
          -1,
					build.getStartTimeInMillis(),
					build.getEstimatedDuration(),
					CIEventCausesFactory.processCauses(build.getCauses()),
					ParameterProcessors.getInstances(build)
			);
      ParameterInstance[] parameters = event.getParameters();
      for(int i = 0 ; i < parameters.length; i++) {
        logger.info("%%%%%%%%%%%%%%% Project Parameter:"+parameters[i].getName() + ":"+parameters[i].getValue());
      }
		}
    if(event != null) {
      EventsDispatcher.getExtensionInstance().dispatchEvent(event);
    }
	}


  private String getProjectName(Run r) {
    if(r.getParent() instanceof MatrixConfiguration){
      return ((MatrixRun) r).getParentBuild().getParent().getName();
    }
    return ((AbstractBuild)r).getProject().getName();
  }

  private List<Cause> listOfCauses(Run r) {
    if(r.getParent() instanceof MatrixConfiguration){
      return ((MatrixRun) r).getParentBuild().getCauses();
    }
    return r.getCauses();
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
          getProjectName(r),
					build.getNumber(),
          -1,
					build.getStartTimeInMillis(),
					build.getEstimatedDuration(),
   				CIEventCausesFactory.processCauses(listOfCauses(build)),
					ParameterProcessors.getInstances(build),
					result,
					build.getDuration(),
					SCMProcessors
							.getAppropriate(build.getProject().getScm().getClass().getName())
							.getSCMChanges(build)
			);
			EventsDispatcher.getExtensionInstance().dispatchEvent(event);

			testListener.processBuild(build);
		}
	}
}
