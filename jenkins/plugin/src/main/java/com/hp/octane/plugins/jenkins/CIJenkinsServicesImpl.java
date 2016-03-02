package com.hp.octane.plugins.jenkins;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.general.*;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.exceptions.JenkinsRequestException;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import hudson.ProxyConfiguration;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.AccessDeniedException2;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by gullery on 21/01/2016.
 * <p>
 * Jenkins CI Server oriented extension of CI Data Provider
 */

public class CIJenkinsServicesImpl implements CIPluginServices {
    private static final Logger logger = Logger.getLogger(CIJenkinsServicesImpl.class.getName());
    private static final DTOFactory dtoFactory = DTOFactory.getInstance();
    private SecurityContext originalContext = null;
    private User jenkinsUser = null;

    @Override
    public CIServerInfo getServerInfo() {
        CIServerInfo result = dtoFactory.newDTO(CIServerInfo.class);
        String serverUrl = Jenkins.getInstance().getRootUrl();
        if (serverUrl != null && serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        result.setType(CIServerTypes.JENKINS)
                .setVersion(Jenkins.VERSION)
                .setUrl(serverUrl)
                .setInstanceId(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity())
                .setInstanceIdFrom(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom())
                .setSendingTime(System.currentTimeMillis());
        return result;
    }

    @Override
    public CIPluginInfo getPluginInfo() {
        CIPluginInfo result = dtoFactory.newDTO(CIPluginInfo.class);
        result.setVersion(Jenkins.getInstance().getPlugin(OctanePlugin.class).getWrapper().getVersion());
        return result;
    }

    @Override
    public File getAllowedNGAStorage() {
        return new File(Jenkins.getInstance().getRootDir(), "userContent" + File.separator + "nga");
    }

    @Override
    public NGAConfiguration getNGAConfiguration() {
        ServerConfiguration serverConfiguration = Jenkins.getInstance().getPlugin(OctanePlugin.class).getServerConfiguration();
        Long sharedSpace = null;
        if (serverConfiguration.sharedSpace != null) {
            try {
                sharedSpace = Long.parseLong(serverConfiguration.sharedSpace);
            } catch (NumberFormatException nfe) {
                logger.severe("found shared space '" + serverConfiguration.sharedSpace + "' yet it's not parsable into Long: " + nfe.getMessage());
            }
        }
        return dtoFactory.newDTO(NGAConfiguration.class)
                .setUrl(serverConfiguration.location)
                .setSharedSpace(sharedSpace)
                .setClientId(serverConfiguration.username)
                .setApiKey(serverConfiguration.password);
    }

    @Override
    public CIProxyConfiguration getProxyConfiguration() {
        CIProxyConfiguration result = null;
        ProxyConfiguration proxy = Jenkins.getInstance().proxy;
        if (proxy != null) {
            result = dtoFactory.newDTO(CIProxyConfiguration.class)
                    .setHost(proxy.name)
                    .setPort(proxy.port)
                    .setUsername(proxy.getUserName())
                    .setPassword(proxy.getPassword());
        }
        return result;
    }

    @Override
    public CIJobsList getJobsList(boolean includeParameters) {
        startImpersonation();
        CIJobsList result = dtoFactory.newDTO(CIJobsList.class);
        CIJobMetadata tmpConfig;
        AbstractProject tmpProject;
        List<CIJobMetadata> list = new ArrayList<CIJobMetadata>();
        try {
            List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
            for (String name : itemNames) {
                tmpProject = (AbstractProject) Jenkins.getInstance().getItem(name);
                tmpConfig = dtoFactory.newDTO(CIJobMetadata.class);
                tmpConfig.setName(name);
                tmpConfig.setCiId(name);
                if (includeParameters) {
                    List<ParameterConfig> tmpList = ParameterProcessors.getConfigs(tmpProject);
                    List<com.hp.nga.integrations.dto.parameters.ParameterConfig> configs = new ArrayList<com.hp.nga.integrations.dto.parameters.ParameterConfig>();
                    for (ParameterConfig pc : tmpList) {
                        configs.add(new com.hp.nga.integrations.dto.parameters.ParameterConfig(
                                pc.getType(),
                                pc.getName(),
                                pc.getDescription(),
                                pc.getDefaultValue(),
                                pc.getChoices() == null ? null : pc.getChoices()
                        ));
                    }
                    tmpConfig.setParameters(configs.toArray(new com.hp.nga.integrations.dto.parameters.ParameterConfig[configs.size()]));
                }
                list.add(tmpConfig);
            }
            result.setJobs(list.toArray(new CIJobMetadata[list.size()]));
            stopImpersonation();
        } catch (AccessDeniedException e) {
            throw new JenkinsRequestException(403);
        }
        return result;
    }

    @Override
    public PipelineNode getPipeline(String rootCIJobId) {
        AbstractProject project = getJobByRefId(rootCIJobId);
        if (project != null) {
            return ModelFactory.createStructureItem(project);
        }
        //todo: check error message(s)
        logger.warning("Failed to get project from jobRefId: '" + rootCIJobId + "' check plugin user Job Read/Overall Read permissions / project name");
        throw new JenkinsRequestException(404);
    }


    private void doImpersonation(boolean enforce) {
        String user = Jenkins.getInstance().getPlugin(OctanePlugin.class).getImpersonatedUser();
        if (user != null && !user.equalsIgnoreCase("")) {
            jenkinsUser = User.get(user, false);
            if (jenkinsUser != null) {
                if (enforce) {
                    logger.info("impersonating with user: " + user);
                    originalContext = ACL.impersonate(jenkinsUser.impersonate());
                } else {
                    if (originalContext != null) {
                        logger.info("un-impersonating back from user: " + user);
                        ACL.impersonate(originalContext.getAuthentication());
                    } else {
                        logger.warning("Could not roll back impersonation, originalContext is null ");
                    }

                }
            } else {
                throw new JenkinsRequestException(401);
            }
        } else {
            logger.info("No user set to impersonating to. Operations will be done using Anonymous user");
        }

    }

    private void startImpersonation() {
        doImpersonation(true);
    }

    private void stopImpersonation() {
        doImpersonation(false);
    }

    private int checkPermssion(AbstractProject project) {
        SecurityContext originalContext = null;
        String user = Jenkins.getInstance().getPlugin(OctanePlugin.class).getImpersonatedUser();
        if (user != null && !user.isEmpty()) {
            User jenkinsUser;
            try {
                jenkinsUser = User.get(user, false);
                if (jenkinsUser == null) {
                    logger.severe("Failed to load user details: " + user);
                    return 401;
                }
            } catch (Exception e) {
                logger.severe("Failed to load user details: " + user);
                return 500;
            }
            try {
                originalContext = ACL.impersonate(jenkinsUser.impersonate());
            } catch (UsernameNotFoundException unfe) {
                logger.severe("Failed to impersonate '" + user + "':" + unfe.getMessage());
                return 402;
            }
        }

        try {
            if (project != null) {
                Jenkins.getInstance().hasPermission(Item.DISCOVER);
                project.checkPermission(Item.BUILD);
            } else {
                return 404;
            }
        } catch (AccessDeniedException2 accessDeniedException) {
            logger.severe(accessDeniedException.getMessage());
            if (user != null && !user.isEmpty()) {
                return 403;
            } else {
                return 405;
            }
        }
        if (originalContext != null) {
            ACL.impersonate(originalContext.getAuthentication());
        }
        return 200;

    }

    @Override
    public int runPipeline(String ciJobId, String originalBody) {
        AbstractProject project = getJobByRefId(ciJobId);
        if (project != null) {
            return doRunImpl(project, originalBody);
        } else {
            throw new JenkinsRequestException(404);
        }
    }

    @Override
    public SnapshotNode getSnapshotLatest(String ciJobId, boolean subTree) {
        SnapshotNode result = null;
        AbstractProject project = getJobByRefId(ciJobId);
        if (project != null) {
            AbstractBuild build = project.getLastBuild();
            if (build != null) {
                result = ModelFactory.createSnapshotItem(build, subTree);
            }
        }
        return result;
    }

    @Override
    public SnapshotNode getSnapshotByNumber(String ciJobId, Integer ciBuildNumber, boolean subTree) {
        startImpersonation();
        AbstractProject project = getJobByRefId(ciJobId);
        if (project != null) {
            AbstractBuild build = project.getBuildByNumber(ciBuildNumber);
            return ModelFactory.createSnapshotItem(build, subTree);
        }
        stopImpersonation();
        return null;
    }

    @Override
    public BuildHistory getHistoryPipeline(String ciJobId, String originalBody) {
        startImpersonation();
        AbstractProject project = getJobByRefId(ciJobId);

        //checkPermssion(project);


        SCMData scmData;
        Set<User> users;
        SCMProcessor scmProcessor = SCMProcessors.getAppropriate(project.getScm().getClass().getName());
        BuildHistory buildHistory = dtoFactory.newDTO(BuildHistory.class);
        int numberOfBuilds = 5;
//		if (req.getParameter("numberOfBuilds") != null) {
//			numberOfBuilds = Integer.valueOf(req.getParameter("numberOfBuilds"));
//		}
        //TODO : check if it works!!
        if (originalBody != null && !originalBody.isEmpty()) {
            JSONObject bodyJSON = JSONObject.fromObject(originalBody);
            if (bodyJSON.has("numberOfBuilds")) {
                numberOfBuilds = bodyJSON.getInt("numberOfBuilds");
            }
        }
        List<Run> result = project.getLastBuildsOverThreshold(numberOfBuilds, Result.FAILURE); // get last five build with result that better or equal failure
        for (int i = 0; i < result.size(); i++) {
            AbstractBuild build = (AbstractBuild) result.get(i);
            scmData = null;
            users = null;
            if (build != null) {
                if (scmProcessor != null) {
                    scmData = scmProcessor.getSCMData(build);
                    users = build.getCulprits();
                }

                buildHistory.addBuild(build.getResult().toString(), String.valueOf(build.getNumber()), build.getTimestampString(), String.valueOf(build.getStartTimeInMillis()), String.valueOf(build.getDuration()), scmData, ModelFactory.createScmUsersList(users));
            }
        }
        AbstractBuild lastSuccessfulBuild = (AbstractBuild) project.getLastSuccessfulBuild();
        if (lastSuccessfulBuild != null) {
            scmData = null;
            users = null;
            if (scmProcessor != null) {
                scmData = scmProcessor.getSCMData(lastSuccessfulBuild);
                users = lastSuccessfulBuild.getCulprits();
            }
            buildHistory.addLastSuccesfullBuild(lastSuccessfulBuild.getResult().toString(), String.valueOf(lastSuccessfulBuild.getNumber()), lastSuccessfulBuild.getTimestampString(), String.valueOf(lastSuccessfulBuild.getStartTimeInMillis()), String.valueOf(lastSuccessfulBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
        }
        AbstractBuild lastBuild = project.getLastBuild();
        if (lastBuild != null) {
            scmData = null;
            users = null;
            if (scmProcessor != null) {
                scmData = scmProcessor.getSCMData(lastBuild);
                users = lastBuild.getCulprits();
            }

            if (lastBuild.getResult() == null) {
                buildHistory.addLastBuild("building", String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(), String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
            } else {
                buildHistory.addLastBuild(lastBuild.getResult().toString(), String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(), String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
            }
        }
        stopImpersonation();
        return buildHistory;
    }

    private int doRunImpl(AbstractProject project, String originalBody) {
        startImpersonation();
        int delay = project.getQuietPeriod();
        ParametersAction parametersAction = new ParametersAction();

        if (originalBody != null && !originalBody.isEmpty()) {
            JSONObject bodyJSON = JSONObject.fromObject(originalBody);

            //  delay
            if (bodyJSON.has("delay") && bodyJSON.get("delay") != null) {
                delay = bodyJSON.getInt("delay");
            }

            //  parameters
            if (bodyJSON.has("parameters") && bodyJSON.get("parameters") != null) {
                JSONArray paramsJSON = bodyJSON.getJSONArray("parameters");
                parametersAction = new ParametersAction(createParameters(project, paramsJSON));
            }
        }

        boolean success = project.scheduleBuild(delay, new Cause.RemoteCause(getNGAConfiguration().getUrl(), "octane driven execution"), parametersAction);
        stopImpersonation();
        if (success) {
            return 201;
        } else {
            return 500;
        }
    }

    private List<ParameterValue> createParameters(AbstractProject project, JSONArray paramsJSON) {
        startImpersonation();
        List<ParameterValue> result = new ArrayList<ParameterValue>();
        boolean parameterHandled;
        ParameterValue tmpValue;
        ParametersDefinitionProperty paramsDefProperty = (ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class);
        if (paramsDefProperty != null) {
            for (ParameterDefinition paramDef : paramsDefProperty.getParameterDefinitions()) {
                parameterHandled = false;
                for (int i = 0; i < paramsJSON.size(); i++) {
                    JSONObject paramJSON = paramsJSON.getJSONObject(i);
                    if (paramJSON.has("name") && paramJSON.get("name") != null && paramJSON.get("name").equals(paramDef.getName())) {
                        tmpValue = null;
                        switch (ParameterType.fromValue(paramJSON.getString("type"))) {
                            case FILE:
                                try {
                                    FileItemFactory fif = new DiskFileItemFactory();
                                    FileItem fi = fif.createItem(paramJSON.getString("name"), "text/plain", false, paramJSON.getString("file"));
                                    fi.getOutputStream().write(DatatypeConverter.parseBase64Binary(paramJSON.getString("value")));
                                    tmpValue = new FileParameterValue(paramJSON.getString("name"), fi);
                                } catch (IOException ioe) {
                                    logger.warning("failed to process file parameter");
                                }
                                break;
                            case NUMBER:
                                tmpValue = new StringParameterValue(paramJSON.getString("name"), paramJSON.get("value").toString());
                                break;
                            case STRING:
                                tmpValue = new StringParameterValue(paramJSON.getString("name"), paramJSON.getString("value"));
                                break;
                            case BOOLEAN:
                                tmpValue = new BooleanParameterValue(paramJSON.getString("name"), paramJSON.getBoolean("value"));
                                break;
                            case PASSWORD:
                                tmpValue = new PasswordParameterValue(paramJSON.getString("name"), paramJSON.getString("value"));
                                break;
                            default:
                                break;
                        }
                        if (tmpValue != null) {
                            result.add(tmpValue);
                            parameterHandled = true;
                        }
                        break;
                    }
                }
                if (!parameterHandled) {
                    if (paramDef instanceof FileParameterDefinition) {
                        FileItemFactory fif = new DiskFileItemFactory();
                        FileItem fi = fif.createItem(paramDef.getName(), "text/plain", false, "");
                        try {
                            fi.getOutputStream().write(new byte[0]);
                        } catch (IOException ioe) {
                            logger.severe("failed to create default value for file parameter '" + paramDef.getName() + "'");
                        }
                        tmpValue = new FileParameterValue(paramDef.getName(), fi);
                        result.add(tmpValue);
                    } else {
                        result.add(paramDef.getDefaultParameterValue());
                    }
                }
            }
        }

        stopImpersonation();
        return result;
    }

    private AbstractProject getJobByRefId(String jobRefId) {
        startImpersonation();
        AbstractProject result = null;

        if (jobRefId != null) {
            try {
                jobRefId = URLDecoder.decode(jobRefId, "UTF-8");
                TopLevelItem item = null;
                item = getTopLevelItem(jobRefId);
                if (item != null && item instanceof AbstractProject) {
                    result = (AbstractProject) item;
                }
            } catch (UnsupportedEncodingException e) {
                logger.severe("failed to decode job ref ID '" + jobRefId + "'");
            }
        }
        stopImpersonation();
        return result;
    }

    private TopLevelItem getTopLevelItem(String jobRefId) {
        TopLevelItem item;
        try {
            item = Jenkins.getInstance().getItem(jobRefId);
        } catch (AccessDeniedException e) {
            String user = Jenkins.getInstance().getPlugin(OctanePlugin.class).getImpersonatedUser();
            if (user != null && !user.isEmpty()) {
                throw new JenkinsRequestException(403);
            } else {
                throw new JenkinsRequestException(405);
            }
        }
        return item;
    }
}
