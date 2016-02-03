//package com.hp.octane.plugins.jenkins.actions;
//
//import hudson.Extension;
//import hudson.model.AbstractProject;
//import hudson.model.Action;
//import hudson.model.ProminentProjectAction;
//import hudson.model.TransientProjectActionFactory;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.logging.Logger;
//
///**
// * Created with IntelliJ IDEA.
// * User: gullery
// * Date: 12/08/14
// * Time: 10:46
// * To change this template use File | Settings | File Templates.
// */
//
//@Extension
//public class ProjectActions extends TransientProjectActionFactory {
//	private static final Logger logger = Logger.getLogger(ProjectActions.class.getName());
//
//	static final public class OctaneProjectActions implements ProminentProjectAction {
//		private static final Logger logger = Logger.getLogger(OctaneProjectActions.class.getName());
//		private AbstractProject project;
//
//		public OctaneProjectActions(AbstractProject p) {
//			project = p;
//		}
//
//		public String getIconFileName() {
//			return null;
//		}
//
//		public String getDisplayName() {
//			return null;
//		}
//
//		public String getUrlName() {
//			return "octane";
//		}
//
////		public void doHistory(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
////			SCMData scmData;
////			Set<User> users;
////			SCMProcessor scmProcessor = SCMProcessors.getAppropriate(project.getScm().getClass().getName());
////			BuildHistory buildHistory = new BuildHistory();
////			int numberOfBuilds = 5;
////			if (req.getParameter("numberOfBuilds") != null) {
////				numberOfBuilds = Integer.valueOf(req.getParameter("numberOfBuilds"));
////			}
////			List<Run> result = project.getLastBuildsOverThreshold(numberOfBuilds, Result.FAILURE); // get last five build with result that better or equal failure
////			for (int i = 0; i < result.size(); i++) {
////				AbstractBuild build = (AbstractBuild) result.get(i);
////				scmData = null;
////				users = null;
////				if (build != null) {
////					if (scmProcessor != null) {
////						scmData = scmProcessor.getSCMData(build);
////						users = build.getCulprits();
////					}
////
////					buildHistory.addBuild(build.getResult().toString(), String.valueOf(build.getNumber()), build.getTimestampString(), String.valueOf(build.getStartTimeInMillis()), String.valueOf(build.getDuration()), scmData, ModelFactory.createScmUsersList(users));
////				}
////			}
////			AbstractBuild lastSuccessfulBuild = (AbstractBuild) project.getLastSuccessfulBuild();
////			if (lastSuccessfulBuild != null) {
////				scmData = null;
////				users = null;
////				if (scmProcessor != null) {
////					scmData = scmProcessor.getSCMData(lastSuccessfulBuild);
////					users = lastSuccessfulBuild.getCulprits();
////				}
////				buildHistory.addLastSuccesfullBuild(lastSuccessfulBuild.getResult().toString(), String.valueOf(lastSuccessfulBuild.getNumber()), lastSuccessfulBuild.getTimestampString(), String.valueOf(lastSuccessfulBuild.getStartTimeInMillis()), String.valueOf(lastSuccessfulBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
////			}
////			AbstractBuild lastBuild = project.getLastBuild();
////			if (lastBuild != null) {
////				scmData = null;
////				users = null;
////				if (scmProcessor != null) {
////					scmData = scmProcessor.getSCMData(lastBuild);
////					users = lastBuild.getCulprits();
////				}
////
////				if (lastBuild.getResult() == null) {
////					buildHistory.addLastBuild("building", String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(), String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
////				} else {
////					buildHistory.addLastBuild(lastBuild.getResult().toString(), String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(), String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
////				}
////			}
////			res.serveExposedBean(req, buildHistory, Flavor.JSON);
////		}
//	}
//
//	@Override
//	public Collection<? extends Action> createFor(AbstractProject project) {
//		ArrayList<Action> actions = new ArrayList<Action>();
//		actions.add(new OctaneProjectActions(project));
//		return actions;
//	}
//}
