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

public class ModelCommonFactory {
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
			treeRoot = dtoFactory.newDTO(PipelineNode.class)
					.setJobCiId(root.getExternalId())
					.setName(root.getName())
					.setParameters(parametersFactory.obtainFromBuildType(root));

			List<PipelineNode> pipelineNodeList = buildFromDependenciesFlat(root.getOwnDependencies());
			if (!pipelineNodeList.isEmpty()) {
				PipelinePhase phase = dtoFactory.newDTO(PipelinePhase.class)
						.setName("teamcity_dependencies")
						.setBlocking(true)
						.setJobs(pipelineNodeList);
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
