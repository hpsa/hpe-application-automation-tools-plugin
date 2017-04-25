package com.hp.application.automation.tools.octane.executor;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.hp.octane.integrations.dto.executor.TestConnectivityInfo;
import com.hp.octane.integrations.dto.scm.SCMType;
import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import hudson.plugins.git.Messages;
import hudson.plugins.git.UserRemoteConfig;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import java.io.IOException;

/**
 * Created by shitritn on 4/23/2017.
 */
public class ExecutorConnTestService {

	public static boolean checkRepositoryConnectivity(TestConnectivityInfo testConnectivityInfo) {
		if(testConnectivityInfo.getScmRepository()!=null && testConnectivityInfo.getScmRepository().getUrl() != null && !testConnectivityInfo.getScmRepository().getUrl().isEmpty()){
			if(testConnectivityInfo.getScmRepository().getType() == SCMType.GIT){
				BaseStandardCredentials c = null;
				if(testConnectivityInfo.getUsername() != null && !testConnectivityInfo.getUsername().isEmpty()
					&& testConnectivityInfo.getPassword() != null ){
					c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,null,null,testConnectivityInfo.getUsername(),testConnectivityInfo.getPassword());
				}
				UserRemoteConfig.DescriptorImpl userRemoteConfig = new UserRemoteConfig.DescriptorImpl();
				try {
//						FormValidation res = userRemoteConfig.doCheckUrl(null,c != null ? c.getId() : "",testConnectivityInfo.getScmRepository().getUrl());

					EnvVars environment = new EnvVars(System.getenv());
					GitClient git = Git.with(TaskListener.NULL, environment).using(GitTool.getDefaultInstallation().getGitExe()).getClient();
					git.addDefaultCredentials(c);

					try {
						git.getHeadRev(testConnectivityInfo.getScmRepository().getUrl(), "HEAD");
					} catch (GitException e) {
						return false;
					}
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
						e.printStackTrace();
				}
			}
		}
		return false;
	}
}
