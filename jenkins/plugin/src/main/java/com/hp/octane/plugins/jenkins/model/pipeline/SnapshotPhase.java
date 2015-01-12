package com.hp.octane.plugins.jenkins.model.pipeline;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.CauseAction;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class SnapshotPhase extends AbstractPhase {
	private SnapshotItem[] items;

	//  TODO: handle the case where invoker equals null: meaning phase of empty slots
	public SnapshotPhase(AbstractBuild build, StructurePhase structurePhase) {
		super(structurePhase.getName(), structurePhase.getBlocking());
		AbstractProject tmpProject;
		AbstractBuild tmpBuild;
		StructureItem[] structures = (StructureItem[]) structurePhase.getJobs();
		items = new SnapshotItem[structures.length];
		for (int i = 0; i < items.length; i++) {
			tmpProject = (AbstractProject) Jenkins.getInstance().getItem(structures[i].getName());
			if (tmpProject != null) {
				tmpBuild = getInvokee(build, tmpProject);
				if (tmpBuild != null) {
					items[i] = new SnapshotItem(tmpBuild);
				} else {
					items[i] = new SnapshotItem(tmpProject);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private AbstractBuild getInvokee(AbstractBuild invoker, AbstractProject project) {
		AbstractBuild tmpBuild;
		Cause.UpstreamCause tmpCause;
		int relation;
		if (invoker == null) return null;
		for (Iterator i = project.getBuilds().iterator(); i.hasNext(); ) {
			if (!(i.next() instanceof AbstractBuild)) continue;

			tmpBuild = (AbstractBuild) i.next();
			for (Cause cause : (List<Cause>) tmpBuild.getCauses()) {
				if (!(cause instanceof Cause.UpstreamCause)) continue;

				tmpCause = (Cause.UpstreamCause) cause;
				if (tmpCause.pointsTo(invoker))
					return tmpBuild;
				if (tmpCause.pointsTo(invoker.getProject()) &&
						tmpCause.getUpstreamBuild() < invoker.getNumber())
					return null;
			}
		}
		return null;
	}

	private int getInterRelation(AbstractBuild invokee, AbstractBuild invoker) {
		Cause.UpstreamCause uCause;
		for (Cause cause : (List<Cause>) invokee.getCauses()) {
			if (cause instanceof Cause.UpstreamCause) {
				uCause = (Cause.UpstreamCause) cause;
				if (uCause.pointsTo(invoker)) return 0;
				if (uCause.pointsTo(invoker.getProject()) && (uCause.getUpstreamBuild() < invoker.getNumber()))
					return -1;
			}
		}
		return 1;
	}

	@Override
	AbstractItem[] provideItems() {
		return items;
	}
}