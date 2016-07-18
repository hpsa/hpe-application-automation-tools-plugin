package com.hp.octane.plugins.jenkins.model;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.parameters.CIParameterType;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.pipelines.PipelinePhase;
import com.hp.nga.integrations.dto.snapshots.*;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import hudson.model.*;
import jenkins.model.Jenkins;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by lazara on 26/01/2016.
 */
public class ModelFactory {
	private static final Logger logger = Logger.getLogger(ModelFactory.class.getName());
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	public static PipelineNode createStructureItem(Job project) {
		PipelineNode pipelineNode = dtoFactory.newDTO(PipelineNode.class);
		pipelineNode.setJobCiId(project.getName());
		pipelineNode.setName(project.getName());
		pipelineNode.setParameters(ParameterProcessors.getConfigs(project));

		AbstractProjectProcessor projectProcessor = AbstractProjectProcessor.getFlowProcessor(project);
		pipelineNode.setPhasesInternal(projectProcessor.getInternals());
		pipelineNode.setPhasesPostBuild(projectProcessor.getPostBuilds());

		return pipelineNode;
	}

	public static PipelinePhase createStructurePhase(String name, boolean blocking, List<AbstractProject> items) {
		PipelinePhase pipelinePhase = dtoFactory.newDTO(PipelinePhase.class);
		pipelinePhase.setName(name);
		pipelinePhase.setBlocking(blocking);

		PipelineNode[] tmp = new PipelineNode[items.size()];
		for (int i = 0; i < tmp.length; i++) {
			if (items.get(i) != null) {
				tmp[i] = ModelFactory.createStructureItem(items.get(i));

			} else {
				logger.warning("One of referenced jobs is null, your Jenkins config probably broken, skipping this job...");
			}
		}

		pipelinePhase.setJobs(Arrays.asList(tmp));

		return pipelinePhase;
	}

	/**
	 * *****************************************************
	 */

	public static SnapshotNode createSnapshotItem(AbstractBuild build, boolean metaOnly) {
		SnapshotNode snapshotNode = dtoFactory.newDTO(SnapshotNode.class);
		SCMProcessor scmProcessor = SCMProcessors.getAppropriate(build.getProject().getScm().getClass().getName());

		CIBuildStatus status = CIBuildStatus.FINISHED;
		if (build.hasntStartedYet()) {
			status = CIBuildStatus.QUEUED;
		} else if (build.isBuilding()) {
			status = CIBuildStatus.RUNNING;
		}

		CIBuildResult result = CIBuildResult.UNAVAILABLE;
		if (build.getResult() == Result.SUCCESS) {
			result = CIBuildResult.SUCCESS;
		} else if (build.getResult() == Result.ABORTED) {
			result = CIBuildResult.ABORTED;
		} else if (build.getResult() == Result.FAILURE) {
			result = CIBuildResult.FAILURE;
		} else if (build.getResult() == Result.UNSTABLE) {
			result = CIBuildResult.UNSTABLE;
		}

		if (!metaOnly) {
			AbstractProjectProcessor flowProcessor = AbstractProjectProcessor.getFlowProcessor(build.getProject());
			List<PipelinePhase> tmpPipelinePhasesInternals = flowProcessor.getInternals();
			List<PipelinePhase> tmpPipelinePhasesPostBuilds = flowProcessor.getPostBuilds();
			ArrayList<String> invokeesNames = new ArrayList<String>();
			appendInvokeesNames(invokeesNames, tmpPipelinePhasesInternals);
			appendInvokeesNames(invokeesNames, tmpPipelinePhasesPostBuilds);
			HashMap<String, ArrayList<AbstractBuild>> invokedBuilds = getInvokedBuilds(build, invokeesNames);
			snapshotNode.setPhasesInternal((inflatePhases(tmpPipelinePhasesInternals, invokedBuilds)));
			snapshotNode.setPhasesPostBuild(inflatePhases(tmpPipelinePhasesPostBuilds, invokedBuilds));
		}

		snapshotNode.setJobCiId(build.getProject().getName());
		snapshotNode.setName(build.getProject().getName());
		snapshotNode.setBuildCiId(String.valueOf(build.getNumber()));
		snapshotNode.setNumber(String.valueOf(build.getNumber()));
		snapshotNode.setCauses(CIEventCausesFactory.processCauses(build.getCauses()));
		snapshotNode.setDuration(build.getDuration());
		snapshotNode.setEstimatedDuration(build.getEstimatedDuration());
		snapshotNode.setScmData(scmProcessor == null ? null : scmProcessor.getSCMData(build));
		snapshotNode.setStartTime(build.getStartTimeInMillis());
		snapshotNode.setParameters(ParameterProcessors.getInstances(build));
		snapshotNode.setResult(result);
		snapshotNode.setStatus(status);

		return snapshotNode;
	}


	public static SnapshotNode createSnapshotItem(AbstractProject project, boolean metaOnly) {
		SnapshotNode snapshotNode = dtoFactory.newDTO(SnapshotNode.class);

		snapshotNode.setJobCiId(project.getName());
		snapshotNode.setName(project.getName());

		if (!metaOnly) {
			AbstractProjectProcessor flowProcessor = AbstractProjectProcessor.getFlowProcessor(project);
			snapshotNode.setPhasesPostBuild(inflatePhases(flowProcessor.getPostBuilds(), null));
			snapshotNode.setPhasesInternal(inflatePhases(flowProcessor.getInternals(), null));
		}
		return snapshotNode;
	}

	private static void appendInvokeesNames(ArrayList<String> list, List<PipelinePhase> phases) {
		for (PipelinePhase phase : phases) {
			for (PipelineNode item : phase.getJobs()) {
				if (item != null) {
					if (!list.contains(item.getJobCiId())) list.add(item.getJobCiId());
				} else {
					logger.severe("null referenced project encountered; considering it as corrupted configuration and skipping");
				}
			}
		}
	}

	private static HashMap<String, ArrayList<AbstractBuild>> getInvokedBuilds(AbstractBuild self, ArrayList<String> invokeesNames) {
		HashMap<String, ArrayList<AbstractBuild>> result = new HashMap<String, ArrayList<AbstractBuild>>();
		AbstractProject project;
		for (String invokeeName : invokeesNames) {
			project = (AbstractProject) Jenkins.getInstance().getItem(invokeeName);
			result.put(invokeeName, getInvokees(self, project));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<AbstractBuild> getInvokees(AbstractBuild invoker, AbstractProject project) {
		ArrayList<AbstractBuild> result = new ArrayList<AbstractBuild>();
		AbstractBuild tmpBuild;
		Cause.UpstreamCause tmpCause;
		for (Object o : project.getBuilds()) {
			tmpBuild = (AbstractBuild) o;
			for (Cause cause : (List<Cause>) tmpBuild.getCauses()) {
				if (!(cause instanceof Cause.UpstreamCause)) continue;

				tmpCause = (Cause.UpstreamCause) cause;
				if (tmpCause.pointsTo(invoker)) {
					result.add(0, tmpBuild);
				} else if (tmpCause.pointsTo(invoker.getProject()) && tmpCause.getUpstreamBuild() < invoker.getNumber()) {
					return result;
				}
			}
		}
		return result;
	}

	private static List<SnapshotPhase> inflatePhases(List<PipelinePhase> structures, HashMap<String, ArrayList<AbstractBuild>> invokedBuilds) {
		List<SnapshotPhase> phases = new ArrayList<SnapshotPhase>();
		for (int i = 0; i < structures.size(); i++) {
			phases.add(i, createSnapshotPhase(structures.get(i), invokedBuilds));
		}
		return phases;
	}

	public static SnapshotPhase createSnapshotPhase(PipelinePhase pipelinePhase, HashMap<String, ArrayList<AbstractBuild>> invokedBuilds) {
		SnapshotPhase snapshotPhase = dtoFactory.newDTO(SnapshotPhase.class);
		snapshotPhase.setName(pipelinePhase.getName());
		snapshotPhase.setBlocking(pipelinePhase.isBlocking());

		ArrayList<AbstractBuild> tmpBuilds;
		List<PipelineNode> structures = pipelinePhase.getJobs();
		List<SnapshotNode> tmp = new ArrayList<SnapshotNode>();

		for (int i = 0; i < structures.size(); i++) {
			if (structures.get(i) != null) {
				tmpBuilds = invokedBuilds == null ? null : invokedBuilds.get(structures.get(i).getJobCiId());
				if (tmpBuilds == null || tmpBuilds.size() == 0) {
					tmp.add(i, createSnapshotItem((AbstractProject) Jenkins.getInstance().getItem(structures.get(i).getJobCiId()), false));
				} else {
					tmp.add(i, createSnapshotItem(tmpBuilds.get(0), false));
					tmpBuilds.remove(0);
				}
			} else {
				logger.warning("One of referenced jobs is null, your Jenkins config probably broken, skipping the build info for this job...");
			}
		}
		snapshotPhase.setBuilds(tmp);

		return snapshotPhase;
	}

	/**
	 * **************************************************************************************
	 */

	public static BuildHistory.SCMUser createScmUser(User user) {
		BuildHistory.SCMUser scmUser = new BuildHistory.SCMUser();
		scmUser.setDisplayName(user.getDisplayName());
		scmUser.setFullName(user.getFullName());
		scmUser.setId(user.getId());

		return scmUser;
	}

	public static Set<BuildHistory.SCMUser> createScmUsersList(Set<User> users) {
		Set<BuildHistory.SCMUser> userList = new HashSet<BuildHistory.SCMUser>();
		if (users != null) {
			for (User user : users) {
				userList.add(ModelFactory.createScmUser(user));
			}
		}
		return userList;
	}

	/**
	 * ***************************************************************************
	 */
	public static CIParameter createParameterConfig(ParameterDefinition pd) {
		return createParameterConfig(pd, CIParameterType.UNKNOWN, null, null);
	}

	public static CIParameter createParameterConfig(ParameterDefinition pd, CIParameterType type) {
		return createParameterConfig(
				pd,
				type,
				pd.getDefaultParameterValue() == null ? null : pd.getDefaultParameterValue().getValue(),
				null);
	}

	public static CIParameter createParameterConfig(ParameterDefinition pd, CIParameterType type, Object defaultValue) {
		return createParameterConfig(pd, type, defaultValue, null);
	}

	public static CIParameter createParameterConfig(String name, CIParameterType type, List<Object> choices) {
		CIParameter ciParameter = dtoFactory.newDTO(CIParameter.class);
		ciParameter.setName(name);
		ciParameter.setType(type);
		ciParameter.setDescription("");
		ciParameter.setChoices(choices.toArray());
		return ciParameter;
	}

	public static CIParameter createParameterConfig(ParameterDefinition pd, CIParameterType type, Object defaultValue, List<Object> choices) {
		CIParameter ciParameter = dtoFactory.newDTO(CIParameter.class);
		ciParameter.setName(pd.getName());
		ciParameter.setType(type);
		ciParameter.setDescription(pd.getDescription());
		ParameterValue tmp;
		if (type != CIParameterType.UNKNOWN) {
			if (defaultValue != null || type == CIParameterType.PASSWORD) {
				ciParameter.setDefaultValue(defaultValue);
			} else {
				tmp = pd.getDefaultParameterValue();
				ciParameter.setDefaultValue(tmp == null ? "" : tmp.getValue());
			}
			if (choices != null) {
				ciParameter.setChoices(choices.toArray());
			}
		}

		return ciParameter;
	}

	/**
	 * *************************************************************
	 */

	public static CIParameter createParameterInstance(CIParameter pc, ParameterValue value) {
		return dtoFactory.newDTO(CIParameter.class)
				.setName(pc.getName())
				.setType(pc.getType())
				.setDescription(pc.getDescription())
				.setChoices(pc.getChoices())
				.setDescription(pc.getDescription())
				.setDefaultValue(pc.getDefaultValue())
				.setValue(value == null ? null : value.getValue().toString());
	}

	public static String generateSubBuildName(List<CIParameter> parameters) {
		List<CIParameter> sortedList = new ArrayList<CIParameter>();
		for (CIParameter p : parameters) {
			if (p.getType().toString() == CIParameterType.AXIS.toString()) {
				sortedList.add(p);
			}
		}

		Collections.sort(sortedList, new Comparator<CIParameter>() {
			@Override
			public int compare(CIParameter p1, CIParameter p2) {
				return p1.getName().compareTo(p2.getName());
			}
		});

		String subBuildName = "";
		if (sortedList.size() > 0) {
			int i = 0;
			for (; i < sortedList.size() - 1; i++) {
				subBuildName += sortedList.get(i).getName() + "=" + sortedList.get(i).getValue().toString() + ",";
			}
			subBuildName += sortedList.get(i).getName() + "=" + sortedList.get(i).getValue().toString();
		}
		return subBuildName;
	}
}
