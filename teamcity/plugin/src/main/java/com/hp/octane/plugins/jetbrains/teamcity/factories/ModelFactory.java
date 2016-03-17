package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.general.CIJobMetadata;
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

	public CIJobsList CreateProjectList() {
		CIJobsList CIJobsList = dtoFactory.newDTO(CIJobsList.class);
		List<CIJobMetadata> list = new ArrayList<CIJobMetadata>();
		List<String> ids = new ArrayList<String>();

		CIJobMetadata buildConf;
		for (SProject project : ngaPlugin.getProjectManager().getProjects()) {

			List<SBuildType> buildTypes = project.getBuildTypes();
			for (SBuildType buildType : buildTypes) {
				if (!ids.contains(buildType.getInternalId())) {
					ids.add(buildType.getInternalId());
					buildConf = dtoFactory.newDTO(CIJobMetadata.class);
					buildConf.setName(buildType.getName());
					buildConf.setCiId(buildType.getExternalId());
					list.add(buildConf);
				}
			}
		}

		CIJobsList.setJobs(list.toArray(new CIJobMetadata[list.size()]));
		return CIJobsList;
	}

	public PipelineNode createStructure(String buildConfigurationId) {
		SBuildType root = ngaPlugin.getProjectManager().findBuildTypeByExternalId(buildConfigurationId);
		PipelineNode treeRoot = null;
		if (root != null) {
			treeRoot = dtoFactory.newDTO(PipelineNode.class);
			treeRoot.setName(root.getName());
			treeRoot.setCiId(root.getExternalId());

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
					PipelineNode buildItem = dtoFactory.newDTO(PipelineNode.class);
					buildItem.setName(build.getName());
					buildItem.setCiId(build.getExternalId());
					//  TODO: add parameters: build.getParameters()
					result.add(buildItem);
					result.addAll(buildFromDependenciesFlat(build.getDependencies()));
				}
			}
		}
		return result;
	}

	public SnapshotNode createSnapshot(String buildConfigurationId) {
		SBuildType root = ngaPlugin.getProjectManager().findBuildTypeByExternalId(buildConfigurationId);
		SnapshotNode snapshotRoot = null;
		if (root != null) {
			snapshotRoot = createSnapshotItem(root, root.getBuildTypeId());
			createSnapshotPipeline(snapshotRoot, root.getDependencies(), root.getBuildTypeId());
		} else {
			//should update the response?
		}
		return snapshotRoot;
	}

	private void createSnapshotPipeline(SnapshotNode treeRoot, List<Dependency> dependencies, String rootId) {
		if (dependencies == null || dependencies.size() == 0) return;
		SnapshotPhase phase = dtoFactory.newDTO(SnapshotPhase.class);
		phase.setBlocking(true);
		phase.setName("teamcity_dependencies");
		List<SnapshotPhase> snapshotPhaseList = new ArrayList<SnapshotPhase>();
		snapshotPhaseList.add(phase);
		List<SnapshotNode> snapshotNodeList = new ArrayList<SnapshotNode>();
		for (Dependency dependency : dependencies) {
			SBuildType build = dependency.getDependOn();
			SnapshotNode snapshotNode = createSnapshotItem(build, rootId);
			snapshotNodeList.add(snapshotNode);
			createSnapshotPipeline(snapshotNode, build.getDependencies(), rootId);
		}
		phase.setBuilds(snapshotNodeList);
		treeRoot.setPhasesInternal(snapshotPhaseList);
	}

	private SnapshotNode createSnapshotItem(SBuildType build, String rootId) {
		//option 1: the build is running now and need to retrieve the data from the running object
		SnapshotNode snapshotNode = createRunningBuild(build, rootId);
		//option 2: the build in the queue
		if (snapshotNode == null) {
			snapshotNode = createQueueBuild(build, rootId);
		}
		//option 3: the build is finished
		if (snapshotNode == null) {
			snapshotNode = createHistoryBuild(build, rootId);
		}
		return snapshotNode;
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
					.setName(build.getExtendedName())
					.setCiId(build.getExternalId())
					.setDuration(currentBuild.getDuration())
					.setEstimatedDuration(null)
					.setNumber(Integer.parseInt(currentBuild.getBuildNumber()))
					.setStartTime(currentBuild.getStartDate().getTime())
					.setCauses(null)
					.setStatus(CIBuildStatus.FINISHED)
					.setResult(resultFromNativeStatus(currentBuild.getBuildStatus()));
		}

		return result;
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
						.setName(build.getName())
						.setCiId(build.getExternalId())
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
					.setName(build.getName())
					.setCiId(build.getExternalId())
					.setDuration(currentBuild.getDuration())
					.setEstimatedDuration(((SRunningBuild) currentBuild).getDurationEstimate())
					.setNumber(Integer.parseInt(currentBuild.getBuildNumber()))
					.setStartTime(currentBuild.getStartDate().getTime())
					.setCauses(null)
					.setStatus(CIBuildStatus.RUNNING)
					.setResult(CIBuildResult.UNAVAILABLE);
		}

		return result;
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
