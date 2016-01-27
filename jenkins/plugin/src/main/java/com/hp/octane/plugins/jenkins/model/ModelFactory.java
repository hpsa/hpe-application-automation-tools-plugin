package com.hp.octane.plugins.jenkins.model;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.pipelines.StructurePhase;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.nga.integrations.dto.snapshots.SnapshotPhase;
import com.hp.nga.integrations.dto.snapshots.SnapshotResult;
import com.hp.nga.integrations.dto.snapshots.SnapshotStatus;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
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

    public static StructureItem createStructureItem(AbstractProject project) {

        StructureItem structureItem = new StructureItem();
        structureItem.setName(project.getName());
        structureItem.setParameters(ParameterProcessors.getConfigs(project));

        AbstractProjectProcessor projectProcessor = AbstractProjectProcessor.getFlowProcessor(project);
        structureItem.setInternals(projectProcessor.getInternals());
        structureItem.setPostBuilds(projectProcessor.getPostBuilds());

        return structureItem;
    }

    public static StructurePhase createStructurePhase(String name, boolean blocking, List<AbstractProject> items) {

        StructurePhase structurePhase = new StructurePhase();
        structurePhase.setName(name);
        structurePhase.setBlocking(blocking);

        StructureItem[] tmp = new StructureItem[items.size()];
        for (int i = 0; i < tmp.length; i++) {
            if (items.get(i) != null) {
                tmp[i] = ModelFactory.createStructureItem(items.get(i));

            } else {
                logger.warning("One of referenced jobs is null, your Jenkins config probably broken, skipping this job...");
            }
        }

        structurePhase.setJobs(Arrays.asList(tmp));

        return structurePhase;
    }

    /*********************************************************/

    public static SnapshotItem createSnapshotItem(AbstractBuild build, boolean metaOnly){

        SnapshotItem snapshotItem = new SnapshotItem();
        SCMProcessor scmProcessor = SCMProcessors.getAppropriate(build.getProject().getScm().getClass().getName());

        SnapshotStatus status = SnapshotStatus.FINISHED;
        if (build.hasntStartedYet()) {
            status = SnapshotStatus.QUEUED;
        } else if (build.isBuilding()) {
            status = SnapshotStatus.RUNNING;
        }

        SnapshotResult result = SnapshotResult.UNAVAILABLE;
        if (build.getResult() == Result.SUCCESS) {
            result = SnapshotResult.SUCCESS;
        } else if (build.getResult() == Result.ABORTED) {
            result = SnapshotResult.ABORTED;
        } else if (build.getResult() == Result.FAILURE) {
            result = SnapshotResult.FAILURE;
        } else if (build.getResult() == Result.UNSTABLE) {
            result = SnapshotResult.UNSTABLE;
        }

        if (!metaOnly) {
            AbstractProjectProcessor flowProcessor = AbstractProjectProcessor.getFlowProcessor(build.getProject());
            List<StructurePhase> tmpStructurePhasesInternals = flowProcessor.getInternals();
            List<StructurePhase> tmpStructurePhasesPostBuilds = flowProcessor.getPostBuilds();
            ArrayList<String> invokeesNames = new ArrayList<String>();
            appendInvokeesNames(invokeesNames, tmpStructurePhasesInternals);
            appendInvokeesNames(invokeesNames, tmpStructurePhasesPostBuilds);
            HashMap<String, ArrayList<AbstractBuild>> invokedBuilds = getInvokedBuilds(build, invokeesNames);
            snapshotItem.setInternals((inflatePhases(tmpStructurePhasesInternals, invokedBuilds)));
            snapshotItem.setPostBuilds(inflatePhases(tmpStructurePhasesPostBuilds, invokedBuilds));
        }

        snapshotItem.setName(build.getProject().getName());
        snapshotItem.setCauses(CIEventCausesFactory.processCauses(build.getCauses()));
        snapshotItem.setNumber(build.getNumber());
        snapshotItem.setDuration(build.getDuration());
        snapshotItem.setEstimatedDuration(build.getEstimatedDuration());
        snapshotItem.setScmData(scmProcessor == null ? null : scmProcessor.getSCMData(build));
        snapshotItem.setStartTime(timeInUTC(build.getStartTimeInMillis()));
        snapshotItem.setParameters(Arrays.asList(ParameterProcessors.getInstances(build)));
        snapshotItem.setResult(result);
        snapshotItem.setStatus(status);

        return snapshotItem;
    }


    public static SnapshotItem createSnapshotItem(AbstractProject project, boolean metaOnly){
        SnapshotItem snapshotItem = new SnapshotItem();

        snapshotItem.setName(project.getName());
        if (!metaOnly) {
            AbstractProjectProcessor flowProcessor = AbstractProjectProcessor.getFlowProcessor(project);
            snapshotItem.setPostBuilds(inflatePhases(flowProcessor.getPostBuilds(), null));
            snapshotItem.setInternals(inflatePhases(flowProcessor.getInternals(), null));
        }
        return snapshotItem;
    }

    private static void appendInvokeesNames(ArrayList<String> list, List<StructurePhase> phases) {
        for (StructurePhase phase : phases) {
            for (StructureItem item : phase.getJobs()) {
                if (item != null) {
                    if (!list.contains(item.getName())) list.add(item.getName());
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

    private static List<SnapshotPhase> inflatePhases(List<StructurePhase> structures, HashMap<String, ArrayList<AbstractBuild>> invokedBuilds) {
        List<SnapshotPhase> phases = new ArrayList<SnapshotPhase>();
        for (int i = 0; i < structures.size(); i++) {
            phases.add(i,createSnapshotPhase(structures.get(i), invokedBuilds));
        }
        return phases;
    }

    //  TODO: replace with the real method from Utils class
    private static long timeInUTC(long input) {
        return input;
        //return input - Calendar.getInstance().getTimeZone().getRawOffset();
    }


    public static SnapshotPhase createSnapshotPhase(StructurePhase structurePhase, HashMap<String, ArrayList<AbstractBuild>> invokedBuilds) {

        com.hp.nga.integrations.dto.snapshots.SnapshotPhase snapshotPhase = new com.hp.nga.integrations.dto.snapshots.SnapshotPhase();
        snapshotPhase.setName(structurePhase.getName());
        snapshotPhase.setBlocking(structurePhase.isBlocking());

        ArrayList<AbstractBuild> tmpBuilds;
        List<StructureItem> structures = structurePhase.getJobs();
        List<SnapshotItem> tmp = new ArrayList<SnapshotItem>();

        for (int i = 0; i < structures.size(); i++) {
            if (structures.get(i) != null) {
                tmpBuilds = invokedBuilds == null ? null : invokedBuilds.get(structures.get(i).getName());
                if (tmpBuilds == null || tmpBuilds.size() == 0) {
                    tmp.add(i, createSnapshotItem((AbstractProject) Jenkins.getInstance().getItem(structures.get(i).getName()), false));
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
  /******************************************************************************************/

    public static BuildHistory.SCMUser createScmUser(User user){
        BuildHistory.SCMUser scmUser = new BuildHistory.SCMUser();
        scmUser.setDisplayName(user.getDisplayName());
        scmUser.setFullName(user.getFullName());
        scmUser.setId(user.getId());

        return scmUser;
    }

    public static Set<BuildHistory.SCMUser> createScmUsersList(Set<User> users) {
        Set<BuildHistory.SCMUser> userList = ModelFactory.createScmUsersList(users);

        for(User user: users){
            userList.add(ModelFactory.createScmUser(user));
        }
        return userList;
    }

    /*******************************************************************************/
    public static ParameterConfig createParameterConfig(ParameterDefinition pd) {
        return createParameterConfig(pd, ParameterType.UNKNOWN, null, null);
    }

    public static ParameterConfig createParameterConfig(ParameterDefinition pd, ParameterType type) {
        return createParameterConfig(pd, type, null, null);
    }

    public static ParameterConfig createParameterConfig(ParameterDefinition pd, ParameterType type, Object defaultValue) {
        return createParameterConfig(pd, type, defaultValue, null);
    }

    public static ParameterConfig createParameterConfig(String name,ParameterType  type, List<Object> choices) {
        ParameterConfig parameterConfig = new ParameterConfig();
        parameterConfig.setName(name);
        parameterConfig.setType(type);
        parameterConfig.setDescription("");
        parameterConfig.setChoices(choices.toArray());
        return parameterConfig;
    }

    public static ParameterConfig createParameterConfig(ParameterDefinition pd, ParameterType type, Object defaultValue, List<Object> choices) {

        ParameterConfig parameterConfig = new ParameterConfig();
        parameterConfig.setName(pd.getName());
        parameterConfig.setType(type);
        parameterConfig.setDescription(pd.getDescription());
        ParameterValue tmp;
        if (type != ParameterType.UNKNOWN) {
            if (defaultValue != null || type == ParameterType.PASSWORD) {
                parameterConfig.setDefaultValue(defaultValue);
            } else {
                tmp = pd.getDefaultParameterValue();
                parameterConfig.setDefaultValue(tmp == null ? "" : tmp.getValue());
            }
            if(choices!=null) {
                parameterConfig.setChoices(choices.toArray());
            }
        }

        return parameterConfig;
    }

    /*****************************************************************/

    public static ParameterInstance createParameterInstance(ParameterConfig pc, ParameterValue value){
            return new ParameterInstance(pc,value == null ? null : value.getValue().toString());
    }

    public static String generateSubBuildName(ParameterInstance[] parameters){
        List<ParameterInstance> sortedList = new ArrayList<ParameterInstance>();
        for(ParameterInstance p : parameters) {
            if(p.getType().toString() == ParameterType.AXIS.toString()) {
                sortedList.add(p);
            }
        }

        Collections.sort(sortedList, new Comparator<ParameterInstance>() {
            @Override
            public int compare(ParameterInstance p1, ParameterInstance p2) {
                return p1.getName().compareTo(p2.getName());
            }
        });

        String subBuildName = "";
        if(sortedList.size() > 0) {
            int i = 0;
            for (; i < sortedList.size() - 1; i++) {
                subBuildName += sortedList.get(i).getName() + "=" + sortedList.get(i).getValue().toString() + ",";
            }
            subBuildName += sortedList.get(i).getName() + "=" + sortedList.get(i).getValue().toString();
        }
        return subBuildName;
    }
}
