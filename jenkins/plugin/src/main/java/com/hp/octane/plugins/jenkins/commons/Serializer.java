package com.hp.octane.plugins.jenkins.commons;

import com.hp.devops.pipelines.SnapshotResult;
import com.hp.devops.pipelines.SnapshotStatus;
import com.hp.octane.plugins.jenkins.scm.SCMDataFactory;
import com.hp.devops.providers.causes.CIEventCauseBase;
import com.hp.devops.scm.SCMData;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 06/09/14
 * Time: 19:47
 * To change this template use File | Settings | File Templates.
 */
public class Serializer {

	//
	//  Public APIs
	//
	static public JSONObject getJSON(AbstractProject project) {
		return getJSON(project, new HashMap<String, Integer>(), 0, false);
	}

	static public JSONObject getJSON(AbstractBuild build) {
		return getJSON(build, new HashMap<String, Integer>(), 0);
	}

	//
	//  Internals
	//
	static private JSONObject getJSON(AbstractProject project, HashMap<String, Integer> orderMap, int order, boolean buildContext) {
		SCMData tmpSCMData;
		JSONObject r = new JSONObject();
		JSONArray steps = new JSONArray();
		List<AbstractProject> downstreamList;
		r.put("name", project.getName());
		r.put("order", order);
		r.put("steps", steps);
		if (!buildContext) {
			tmpSCMData = SCMDataFactory.create(project);
			if (tmpSCMData != null) r.put("scmData", tmpSCMData.toJSON());
		}

		downstreamList = project.getDownstreamProjects();

		if (project.getClass().getName().compareTo("com.tikal.jenkins.plugins.multijob.MultiJobProject") == 0) {
			appendSteps(project, ((hudson.model.Project) project).getBuilders(), steps, orderMap, null);
			//  TODO: this is HACK to handle the bug in the MultiJob downstream projects definition (all of the jobs are taken as downstream as well)
			//  remove all of the multi job projects from the downstreamList and the rest append as downstream
		} else if (project instanceof hudson.model.Project) {
			appendSteps(project, ((hudson.model.Project) project).getBuilders(), steps, orderMap, "build");
			appendStep(downstreamList, steps, orderMap, "downstream", true);
		} else if (project instanceof MatrixProject) {
			appendSteps(project, ((MatrixProject) project).getBuilders(), steps, orderMap, "build");
			appendStep(downstreamList, steps, orderMap, "downstream", true);
		} else if (project instanceof MavenModuleSet) {
			appendSteps(project, ((MavenModuleSet) project).getPrebuilders(), steps, orderMap, "pre-build");
			appendSteps(project, ((MavenModuleSet) project).getPostbuilders(), steps, orderMap, "post-build");
			appendStep(downstreamList, steps, orderMap, "downstream", true);
		}
		return r;
	}

	static private JSONObject getJSON(AbstractBuild build, HashMap<String, Integer> orderMap, int order) {
		AbstractProject project = build.getProject();
		JSONObject r = getJSON(project, orderMap, order, true);
		inflateBuildData(build, build, r);
		return r;
	}

	static private void appendSteps(AbstractProject project, List<Builder> builders, JSONArray steps, HashMap<String, Integer> orderMap, String phaseName) {
		ArrayList<AbstractProject> subSteps;
		for (Builder builder : builders) {
			if (builder instanceof TriggerBuilder) {
				TriggerBuilder tb = (TriggerBuilder) builder;
				for (BlockableBuildTriggerConfig config : tb.getConfigs()) {
					appendStep(config.getProjectList(project.getParent(), null), steps, orderMap, phaseName, config.getBlock() == null);
				}
			} else if (builder.getClass().getName().compareTo("com.tikal.jenkins.plugins.multijob.MultiJobBuilder") == 0) {
				subSteps = MultiJobUtils.retrieveSubSteps(builder);
				appendStep(subSteps, steps, orderMap, MultiJobUtils.retrievePhaseName(builder), false);
			} else {
				System.out.println("not yet supported build action: " + builder.getClass().getName());
			}
		}
	}

	static private void appendStep(List<AbstractProject> projects, JSONArray steps, HashMap<String, Integer> orderMap, String phaseName, boolean async) {
		if (projects.size() == 0) return;
		JSONObject tmpStep = new JSONObject();
		JSONArray tmpSubSteps = new JSONArray();
		tmpStep.put("phase", phaseName);
		tmpStep.put("async", async);
		inflateSubSteps(projects, tmpSubSteps, orderMap);
		tmpStep.put("subSteps", tmpSubSteps);
		steps.put(tmpStep);
	}

	static private void inflateSubSteps(List<AbstractProject> projects, JSONArray subSteps, HashMap<String, Integer> orderMap) {
		String tmpName;
		for (AbstractProject project : projects) {
			tmpName = project.getName();
			if (!orderMap.containsKey(tmpName)) orderMap.put(tmpName, 0);
			else orderMap.put(tmpName, orderMap.get(tmpName) + 1);
			subSteps.put(getJSON(project, orderMap, orderMap.get(tmpName), false));
		}
	}

	static private void inflateBuildData(AbstractBuild parentBuild, AbstractBuild build, JSONObject json) {
		AbstractProject tmpProject;
		AbstractBuild tmpBuild;
		SnapshotStatus tmpStatus;
		SnapshotResult tmpResult;
		CIEventCauseBase tmpCause;
		String tmpName;
		SCMData tmpSCMData;
		int tmpOrder;
		if (build != null) {
			if (build.hasntStartedYet()) {
				tmpStatus = SnapshotStatus.QUEUED;
			} else if (build.isBuilding()) {
				tmpStatus = SnapshotStatus.RUNNING;
			} else {
				tmpStatus = SnapshotStatus.FINISHED;
			}
			if (build.getResult() == Result.SUCCESS) {
				tmpResult = SnapshotResult.SUCCESS;
			} else if (build.getResult() == Result.ABORTED) {
				tmpResult = SnapshotResult.ABORTED;
			} else if (build.getResult() == Result.FAILURE) {
				tmpResult = SnapshotResult.FAILURE;
			} else if (build.getResult() == Result.UNSTABLE) {
				tmpResult = SnapshotResult.UNSTABLE;
			} else {
				tmpResult = SnapshotResult.UNAVAILABLE;
			}
			json.put("number", build.getNumber());
			json.put("status", tmpStatus.toString());
			json.put("result", tmpResult.toString());
			json.put("startTime", build.getStartTimeInMillis());
			json.put("duration", build.getDuration());
			json.put("estimatedDuration", build.getEstimatedDuration());

			tmpCause = CIEventCausesFactory.convertCause(build.getCauses());
			if (tmpCause != null) {
				json.put("cause", tmpCause.toJSON());
			}

			tmpSCMData = SCMDataFactory.create(build);
			if (tmpSCMData != null) {
				json.put("scmData", tmpSCMData.toJSON());
			}

			for (int i = 0; i < json.getJSONArray("steps").length(); i++) {
				for (int j = 0; j < json.getJSONArray("steps").getJSONObject(i).getJSONArray("subSteps").length(); j++) {
					tmpName = json.getJSONArray("steps").getJSONObject(i).getJSONArray("subSteps").getJSONObject(j).getString("name");
					tmpOrder = json.getJSONArray("steps").getJSONObject(i).getJSONArray("subSteps").getJSONObject(j).getInt("order");
					tmpProject = AbstractProject.findNearest(tmpName);
					if (json.getJSONArray("steps").getJSONObject(i).getString("phase") == "downstream") {
						tmpBuild = getRelatedBuildForDownstream(parentBuild, tmpProject);
						inflateBuildData(tmpBuild, tmpBuild, json.getJSONArray("steps").getJSONObject(i).getJSONArray("subSteps").getJSONObject(j));
					} else {
						tmpBuild = getRelatedBuildForSubproject(parentBuild, tmpProject, tmpOrder);
						inflateBuildData(parentBuild, tmpBuild, json.getJSONArray("steps").getJSONObject(i).getJSONArray("subSteps").getJSONObject(j));
					}
				}

			}
		} else {
			json.put("status", SnapshotStatus.UNAVAILABLE.toString());
		}
	}

	static private AbstractBuild getRelatedBuildForDownstream(AbstractBuild parentBuild, AbstractProject childProject) {
		AbstractBuild tmp;
		int rel;
		for (Iterator<hudson.model.Build> iterator = childProject.getBuilds().iterator(); iterator.hasNext(); ) {
			tmp = iterator.next();
			rel = downstreamBuildsRelation(parentBuild, tmp);
			if (rel == 0) return tmp;
			else if (rel == -1) break;
		}
		return null;
	}

	static private int downstreamBuildsRelation(AbstractBuild proposedParent, AbstractBuild proposedChild) {
		Cause.UpstreamCause uCause;
		for (CauseAction action : proposedChild.getActions(CauseAction.class)) {
			for (Cause cause : action.getCauses()) {
				if (cause instanceof Cause.UpstreamCause) {
					uCause = (Cause.UpstreamCause) cause;
					if (uCause.pointsTo(proposedParent)) return 0;
					if (uCause.pointsTo(proposedParent.getProject()) && (uCause.getUpstreamBuild() < proposedParent.getNumber()))
						return -1;
				}
			}
		}
		return 1;
	}

	static private AbstractBuild getRelatedBuildForSubproject(AbstractBuild parentBuild, AbstractProject childProject, int order) {
		ArrayList<AbstractBuild> list = new ArrayList<AbstractBuild>();
		AbstractBuild r = null;
		AbstractBuild tmp;
		int rel;
		for (Iterator<hudson.model.Build> iterator = childProject.getBuilds().iterator(); iterator.hasNext(); ) {
			tmp = iterator.next();
			rel = subprojectsBuildsRelation(parentBuild, tmp);
			if (rel == 0) list.add(0, tmp);
			else if (rel == -1) break;
		}
		if (order < list.size()) r = list.get(order);
		return r;
	}

	static private int subprojectsBuildsRelation(AbstractBuild proposedParent, AbstractBuild proposedChild) {
		Cause.UpstreamCause uCause;
		for (CauseAction action : proposedChild.getActions(CauseAction.class)) {
			for (Cause cause : action.getCauses()) {
				if (cause instanceof Cause.UpstreamCause) {
					uCause = (Cause.UpstreamCause) cause;
					if (inCauses(uCause, proposedParent)) return 0;
					if (uCause.pointsTo(proposedParent.getProject()) && (uCause.getUpstreamBuild() < proposedParent.getNumber()))
						return -1;
				}
			}
		}
		return 1;
	}

	static private boolean inCauses(Cause.UpstreamCause cause, AbstractBuild build) {
		if (cause.pointsTo(build)) return true;
		else {
			List<Cause> list = cause.getUpstreamCauses();
			for (Cause c : list) {
				if (c instanceof Cause.UpstreamCause) {
					if (inCauses((Cause.UpstreamCause) c, build)) return true;
				}
			}
		}
		return false;
	}
}
