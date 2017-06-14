package com.hpe.application.automation.tools.run;

import com.hpe.application.automation.tools.mc.JobConfigurationProxy;
import com.hpe.application.automation.tools.model.MCServerSettingsModel;
import com.hpe.application.automation.tools.model.ProxySettings;
import com.hpe.application.automation.tools.model.UploadAppModel;
import com.hpe.application.automation.tools.model.UploadAppPathModel;
import com.hpe.application.automation.tools.settings.MCServerSettingsBuilder;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.minidev.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 5/17/16
 * Time: 4:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class UploadAppBuilder extends Builder {

    private final UploadAppModel uploadAppModel;

    @DataBoundConstructor
    public UploadAppBuilder(String mcServerName, String mcUserName, String mcPassword, ProxySettings proxySettings, List<UploadAppPathModel> applicationPaths) {
        uploadAppModel = new UploadAppModel(mcServerName, mcUserName, mcPassword, proxySettings, applicationPaths);
    }

    @Override
    public DescriptorImpl getDescriptor() {

        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        // get the mc server settings
        MCServerSettingsModel mcServerSettingsModel = getMCServerSettingsModel();
        JobConfigurationProxy job = JobConfigurationProxy.getInstance();
        JSONObject app = null;
        String mcServerUrl = "";
        PrintStream out = listener.getLogger();
        List<UploadAppPathModel> paths = null;
        if(uploadAppModel != null){
            paths = uploadAppModel.getApplicationPaths();
        }
        if(mcServerSettingsModel == null){
            out.println("Failed to upload app to MC server. Cause: MC url didn't be configured." );
            return false;
        }else{
            mcServerUrl = mcServerSettingsModel.getProperties().getProperty("MobileHostAddress");
            out.println(String.format("There are %d apps to be uploaded.", paths.size()));
            for(int i=1; i<=paths.size(); i++){
                String path = paths.get(i-1).getMcAppPath();
                try{
                    out.println(String.format("starting to upload app %d %s", i, path));
                    if(uploadAppModel.getProxySettings() == null){
                        app = job.upload(mcServerUrl, uploadAppModel.getMcUserName(),uploadAppModel.getMcPassword(), null, null, null, path);
                    }else{
                        app = job.upload(mcServerUrl, uploadAppModel.getMcUserName(),uploadAppModel.getMcPassword(), uploadAppModel.getProxySettings().getFsProxyAddress(),uploadAppModel.getProxySettings().getFsProxyUserName(), uploadAppModel.getProxySettings().getFsProxyPassword(),path);
                    }
                    if(app == null){
                        if(uploadAppModel.isUseProxy()){
                            out.println(String.format("Failed to upload app, Cause MC connection info is incorrect. url:%s, username:%s, Proxy url:%s",
                                    mcServerUrl, uploadAppModel.getMcUserName(), uploadAppModel.getProxySettings().getFsProxyAddress()));
                        }else if(uploadAppModel.isUseAuthentication()){
                            out.println(String.format("Failed to upload app, Cause MC connection info is incorrect. url:%s, username:%s, Proxy url:%s, proxy userName:%s",
                                    mcServerUrl, uploadAppModel.getMcUserName(), uploadAppModel.getProxySettings().getFsProxyAddress(), uploadAppModel.getProxySettings().getFsProxyUserName()));
                        }else{
                            out.println(String.format("Failed to upload app, Cause MC connection info is incorrect. url:%s, username:%s,",
                                    mcServerUrl, uploadAppModel.getMcUserName()));
                        }
                        build.setResult(Result.FAILURE);
                        return false;
                    }
                    out.println("uploaded app info: " + app.toJSONString());
                } catch(FileNotFoundException fnf){
                    out.println(String.format("Failed to upload app to MC server. Cause: File: %s is not found.", path));
                    build.setResult(Result.FAILURE);
                    return false;
                } catch (IOException ioe) {
                    Util.displayIOException(ioe, listener);
                    build.setResult(Result.FAILURE);
                    return false;
                } catch (InterruptedException e) {
                    build.setResult(Result.ABORTED);
                } catch (Exception e){
                    if(uploadAppModel.isUseProxy()){
                        out.println(String.format("Failed to upload app, Cause MC connection info is incorrect. url:%s, username:%s, Proxy url:%s",
                                mcServerUrl, uploadAppModel.getMcUserName(), uploadAppModel.getProxySettings().getFsProxyAddress()));
                    }else if(uploadAppModel.isUseAuthentication()){
                        out.println(String.format("Failed to upload app, Cause MC connection info is incorrect. url:%s, username:%s, Proxy url:%s, proxy userName:%s",
                                mcServerUrl, uploadAppModel.getMcUserName(), uploadAppModel.getProxySettings().getFsProxyAddress(), uploadAppModel.getProxySettings().getFsProxyUserName()));
                    }else{
                        out.println(String.format("Failed to upload app, Cause MC connection info is incorrect. url:%s, username:%s,",
                                mcServerUrl, uploadAppModel.getMcUserName()));
                    }
                    build.setResult(Result.FAILURE);
                    return false;
                }
            }
        }
        return true;
    }

    public MCServerSettingsModel getMCServerSettingsModel() {
        for (MCServerSettingsModel mcServer : getDescriptor().getMcServers()) {
            if (this.uploadAppModel != null
                    && uploadAppModel.getMcServerName().equals(mcServer.getMcServerName())) {
                return mcServer;
            }
        }
        return null;
    }

    public UploadAppModel getUploadAppModel() {
        return uploadAppModel;
    }

    // This indicates to Jenkins that this is an implementation of an extension point
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {

            load();
        }

        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {

            return true;
        }

        @Override
        public String getDisplayName() {

            return "Upload app to Mobile Center";
        }

        public boolean hasMCServers() {
            return Hudson.getInstance().getDescriptorByType(
                    MCServerSettingsBuilder.MCDescriptorImpl.class).hasMCServers();
        }

        public MCServerSettingsModel[] getMcServers() {
            return Hudson.getInstance().getDescriptorByType(
                    MCServerSettingsBuilder.MCDescriptorImpl.class).getInstallations();
        }
    }
}
