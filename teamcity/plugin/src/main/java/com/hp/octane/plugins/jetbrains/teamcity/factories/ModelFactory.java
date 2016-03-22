package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.pipelines.PipelinePhase;
import com.hp.nga.integrations.dto.snapshots.CIBuildResult;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.dto.snapshots.SnapshotPhase;
import com.hp.nga.integrations.dto.snapshots.CIBuildStatus;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.dependency.Dependency;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lazara on 04/01/2016.
 */

public class ModelFactory {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Autowired
	private NGAPlugin ngaPlugin;
	@Autowired
	private ParametersFactory parametersFactory;

	public CIJobsList CreateProjectList() {
		CIJobsList ciJobsList = dtoFactory.newDTO(CIJobsList.class);
		List<PipelineNode> list = new ArrayList<PipelineNode>();
		List<String> ids = new ArrayList<String>();

		PipelineNode buildConf;
		for (SProject project : ngaPlugin.getProjectManager().getProjects()) {

			List<SBuildType> buildTypes = project.getBuildTypes();
			for (SBuildType buildType : buildTypes) {
				if (!ids.contains(buildType.getInternalId())) {
					ids.add(buildType.getInternalId());
					buildConf = dtoFactory.newDTO(PipelineNode.class)
							.setJobCiId(buildType.getExternalId())
							.setName(buildType.getName());
					list.add(buildConf);
				}
			}
		}

		ciJobsList.setJobs(list.toArray(new PipelineNode[list.size()]));
		return ciJobsList;
	}

	public PipelineNode createStructure(String buildConfigurationId) {
		SBuildType root = ngaPlugin.getProjectManager().findBuildTypeByExternalId(buildConfigurationId);
		PipelineNode treeRoot = null;
		if (root != null) {
			treeRoot = dtoFactory.newDTO(PipelineNode.class);
			treeRoot.setJobCiId(root.getExternalId());
			treeRoot.setName(root.getName());

			List<PipelineNode> pipelineNodeList = buildFromDependenciesFlat(root.getOwnDependencies());
			if (!pipelineNodeList.isEmpty()) {
				PipelinePhase phase = dtoFactory.newDTO(PipelinePhase.class);
				phase.setName("teamcity_dependencies");
				phase.setBlocking(true);
				phase.setJobs(pipelineNodeList);
				List<PipelinePhase> pipelinePhaseList = new ArrayList<PipelinePhase>();
				pipelinePhaseList.add(phase);
				treeRoot.setPhasesPostBuild(pipelinePhaseList);
			}
		} else {
			//should update the response?
		}
		return treeRoot;
	}

	private List<PipelineNode> buildFromDependenciesFlat(List<Dependency> dependencies) {
		List<PipelineNode> result = new LinkedList<PipelineNode>();
		if (dependencies != null) {
			for (Dependency dependency : dependencies) {
				SBuildType build = dependency.getDependOn();
				if (build != null) {
					PipelineNode buildItem = dtoFactory.newDTO(PipelineNode.class)
							.setJobCiId(build.getExternalId())
							.setName(build.getName())
							.setParameters(parametersFactory.obtainFromBuildType(build));
					result.add(buildItem);
					result.addAll(buildFromDependenciesFlat(build.getOwnDependencies()));
				}
			}
		}
		return result;
	}

	public SnapshotNode createSnapshot(String buildConfigurationId) {
		SBuildType root = ngaPlugin.getProjectManager().findBuildTypeByExternalId(buildConfigurationId);
		SnapshotNode result = null;
		if (root != null) {
			result = createSnapshotItem(root, root.getBuildTypeId());

			List<SnapshotNode> snapshotNodesList = createSnapshots(root.getOwnDependencies(), root.getBuildTypeId());
			if (!snapshotNodesList.isEmpty()) {
				SnapshotPhase phase = dtoFactory.newDTO(SnapshotPhase.class)
						.setName("teamcity_dependencies")
						.setBlocking(true)
						.setBuilds(snapshotNodesList);
				List<SnapshotPhase> snapshotPhases = new ArrayList<SnapshotPhase>();
				snapshotPhases.add(phase);
				result.setPhasesPostBuild(snapshotPhases);
			}
		} else {
			//should update the response?
		}
		return result;
	}

	private List<SnapshotNode> createSnapshots(List<Dependency> dependencies, String rootId) {
		List<SnapshotNode> result = new LinkedList<SnapshotNode>();

		if (dependencies != null && !dependencies.isEmpty()) {
			for (Dependency dependency : dependencies) {
				SBuildType build = dependency.getDependOn();
				SnapshotNode snapshotNode = createSnapshotItem(build, rootId);
				result.add(snapshotNode);
				result.addAll(createSnapshots(build.getOwnDependencies(), rootId));
			}
		}

		return result;
	}

	private SnapshotNode createSnapshotItem(SBuildType build, String rootId) {
		//  Option 1: the build is running now and need to retrieve the data from the running object
		SnapshotNode snapshotNode = createRunningBuild(build, rootId);
		//  Option 2: the build in the queue
		if (snapshotNode == null) {
			snapshotNode = createQueueBuild(build, rootId);
		}
		//  Option 3: the build is finished
		if (snapshotNode == null) {
			snapshotNode = createHistoryBuild(build, rootId);
		}
		//  Option 4: if the build not available, create empty build
		if (snapshotNode == null) {
			snapshotNode = createUnavailableBuild(build);
		}
		return snapshotNode;
	}

	private SnapshotNode createQueueBuild(SBuildType build, String rootId) {
		SnapshotNode result = null;

		if (build.isInQueue()) {
			List<SQueuedBuild> queuedBuilds = build.getQueuedBuilds(null);
			SQueuedBuild queuedBuild = null;
			if (build.getBuildTypeId().equalsIgnoreCase(rootId) && queuedBuilds.size() > 0) {
				queuedBuild = queuedBuilds.get(0);
			} else {
				for (SQueuedBuild runningBuild : queuedBuilds) {
					TriggeredBy trigger = runningBuild.getTriggeredBy();
					if (rootId.equalsIgnoreCase(trigger.getParameters().get("buildTypeId"))) {
						queuedBuild = runningBuild;
						break;
					}
				}
			}

			if (queuedBuild != null) {
				result = dtoFactory.newDTO(SnapshotNode.class)
						.setJobCiId(build.getExternalId())
						.setBuildCiId(queuedBuild.getItemId())
						.setName(build.getName())
						.setStatus(CIBuildStatus.QUEUED)
						.setResult(CIBuildResult.UNAVAILABLE);
			}
		}

		return result;
	}

	private SnapshotNode createRunningBuild(SBuildType build, String rootId) {
		SnapshotNode result = null;
		SBuild currentBuild = null;

		List<SRunningBuild> runningBuilds = build.getRunningBuilds();

		if (build.getBuildTypeId().equalsIgnoreCase(rootId) && runningBuilds.size() > 0) {
			currentBuild = runningBuilds.get(0);
		} else {
			for (SBuild runningBuild : runningBuilds) {
				TriggeredBy trigger = runningBuild.getTriggeredBy();
				if (rootId.equalsIgnoreCase(trigger.getParameters().get("buildTypeId"))) {
					currentBuild = runningBuild;
					break;
				}
			}
		}

		if (currentBuild != null) {
			result = dtoFactory.newDTO(SnapshotNode.class)
					.setJobCiId(build.getExternalId())
					.setName(build.getName())
					.setBuildCiId(String.valueOf(currentBuild.getBuildId()))
					.setNumber(currentBuild.getBuildNumber())
					.setParameters(parametersFactory.obtainFromBuild(currentBuild))
					.setDuration(currentBuild.getDuration() * 1000)
					.setEstimatedDuration(((SRunningBuild) currentBuild).getDurationEstimate() * 1000)
					.setStartTime(currentBuild.getStartDate().getTime())
					.setCauses(null)
					.setStatus(CIBuildStatus.RUNNING)
					.setResult(CIBuildResult.UNAVAILABLE);
		}

		return result;
	}

	private SnapshotNode createHistoryBuild(SBuildType build, String rootId) {
		SnapshotNode result = null;
		SBuild currentBuild = null;

		List<SFinishedBuild> finishedBuilds = build.getHistory();

		if (build.getBuildTypeId().equalsIgnoreCase(rootId) && finishedBuilds.size() > 0) {
			currentBuild = finishedBuilds.get(0);
		} else {
			for (SBuild runningBuild : finishedBuilds) {
				TriggeredBy trigger = runningBuild.getTriggeredBy();
				if (trigger.getParameters().get("buildTypeId") != null && rootId.equalsIgnoreCase(trigger.getParameters().get("buildTypeId"))) {
					currentBuild = runningBuild;
					break;
				}
			}
		}

		if (currentBuild != null) {
			result = dtoFactory.newDTO(SnapshotNode.class)
					.setJobCiId(build.getExternalId())
					.setName(build.getExtendedName())
					.setBuildCiId(String.valueOf(currentBuild.getBuildId()))
					.setNumber(currentBuild.getBuildNumber())
					.setParameters(parametersFactory.obtainFromBuild(currentBuild))
					.setDuration(currentBuild.getDuration() * 1000)
					.setEstimatedDuration(currentBuild.getDuration() * 1000)
					.setStartTime(currentBuild.getStartDate().getTime())
					.setCauses(null)
					.setStatus(CIBuildStatus.FINISHED)
					.setResult(resultFromNativeStatus(currentBuild.getBuildStatus()));
		}

		return result;
	}

	private SnapshotNode createUnavailableBuild(SBuildType build) {
		return dtoFactory.newDTO(SnapshotNode.class)
				.setJobCiId(build.getExternalId())
				.setName(build.getExtendedName())
				.setStatus(CIBuildStatus.UNAVAILABLE)
				.setResult(CIBuildResult.UNAVAILABLE);
	}

	public CIBuildResult resultFromNativeStatus(Status status) {
		CIBuildResult result = CIBuildResult.UNAVAILABLE;
		if (status == Status.ERROR || status == Status.FAILURE) {
			result = CIBuildResult.FAILURE;
		} else if (status == Status.WARNING) {
			result = CIBuildResult.UNSTABLE;
		} else if (status == Status.NORMAL) {
			result = CIBuildResult.SUCCESS;
		}
		return result;
	}
}
