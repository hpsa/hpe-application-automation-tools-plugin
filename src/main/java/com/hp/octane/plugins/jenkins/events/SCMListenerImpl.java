package com.hp.octane.plugins.jenkins.events;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.scm.SCMData;
import com.hp.octane.plugins.jenkins.model.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import hudson.Extension;
import hudson.FilePath;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.File;
import java.util.List;

/**
 * Created by gullery on 10/07/2016.
 */

@Extension
public class SCMListenerImpl extends SCMListener {
    private static final Logger logger = LogManager.getLogger(SCMListenerImpl.class);
    private static final DTOFactory dtoFactory = DTOFactory.getInstance();


    @Override
    public void onCheckout(Run<?, ?> build, SCM scm, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState pollingBaseline) throws Exception {
        super.onCheckout(build, scm, workspace, listener, changelogFile, pollingBaseline);
    }

    @Override
    public void onChangeLogParsed(Run<?, ?> r, SCM scm, TaskListener listener, ChangeLogSet<?> changelog) throws Exception {
        super.onChangeLogParsed(r, scm, listener, changelog);
        CIEvent event;
        if (r.getParent() instanceof MatrixConfiguration || r instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) r;
            if (changelog != null && !changelog.isEmptySet()) {        // if there are any commiters
                SCMProcessor scmProcessor = SCMProcessors.getAppropriate(scm.getClass().getName());
                if (scmProcessor != null) {
                    createSCMData(r, build, scmProcessor);
                } else {
                    logger.info("SCM changes detected, but no processors found for SCM provider of type " + scm.getClass().getName());
                }
            }
        }

        else if (r.getParent() instanceof WorkflowJob) {
            WorkflowRun wRun = (WorkflowRun)r;
            if (changelog != null && !changelog.isEmptySet() || !wRun.getChangeSets().isEmpty()) {
                SCMProcessor scmProcessor = SCMProcessors.getAppropriate(scm.getClass().getName());
                if (scmProcessor != null) {
                    List<SCMData> scmDataList = scmProcessor.getSCMData(wRun);
                    for (SCMData scmData : scmDataList) {
                        event = dtoFactory.newDTO(CIEvent.class)
                          .setEventType(CIEventType.SCM)
                          .setProject(getProjectName(r))
                          .setBuildCiId(String.valueOf(r.getNumber()))
                          .setCauses(CIEventCausesFactory.processCauses(extractCauses(r)))
                          .setNumber(String.valueOf(r.getNumber()))
                          .setScmData(scmData);
                        EventsService.getExtensionInstance().dispatchEvent(event);
                    }
                } else {
                    logger.info("SCM changes detected, but no processors found for SCM provider of type " + scm.getClass().getName());
                }
            }
        }
    }

    private void createSCMData(Run<?, ?> r, AbstractBuild build, SCMProcessor scmProcessor) {
        CIEvent event;
        SCMData scmData = scmProcessor.getSCMData(build);
        event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.SCM)
					.setProject(getProjectName(r))
					.setBuildCiId(String.valueOf(r.getNumber()))
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(r)))
					.setNumber(String.valueOf(r.getNumber()))
					.setScmData(scmData);
        EventsService.getExtensionInstance().dispatchEvent(event);
    }

    private String getProjectName(Run r) {
        if (r.getParent() instanceof MatrixConfiguration) {
            return ((MatrixRun) r).getParentBuild().getParent().getName();
        }
        if (r.getParent().getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
            return r.getParent().getName();
        }
        return ((AbstractBuild) r).getProject().getName();
    }


    private List<Cause> extractCauses(Run r) {
        if (r.getParent() instanceof MatrixConfiguration) {
            return ((MatrixRun) r).getParentBuild().getCauses();
        } else {
            return r.getCauses();
        }
    }
}
