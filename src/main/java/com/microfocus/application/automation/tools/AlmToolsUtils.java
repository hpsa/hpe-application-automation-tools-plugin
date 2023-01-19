/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools;

import com.microfocus.application.automation.tools.settings.UFTEncryptionGlobalConfiguration;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.util.ArgumentListBuilder;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("squid:S1160")
public final class AlmToolsUtils {


	private AlmToolsUtils() {
        // no meaning instantiating
	}
    public static void runOnBuildEnv(
            Run<?, ?> build,
            Launcher launcher,
            TaskListener listener,
            FilePath file,
            String paramFileName,
            Node node) throws IOException, InterruptedException {
        runOnBuildEnv(build, launcher, listener, file, paramFileName, node, "UTF-8");
    }
	public static void runOnBuildEnv(
            Run<?, ?> build,
            Launcher launcher,
            TaskListener listener,
            FilePath file,
            String paramFileName,
            Node node,
            String encoding) throws IOException, InterruptedException {

            ArgumentListBuilder args = new ArgumentListBuilder();
            PrintStream out = listener.getLogger();

            // Use script to run the cmdLine and get the console output
            args.add(file);
            args.add("-paramfile");
            args.add(paramFileName);
            if (StringUtils.isNotBlank(encoding)) {
                args.add("-encoding");
                args.add(encoding);
            }

            // for encryption
            Map<String, String> envs = new HashMap<>();

            try {
                UFTEncryptionGlobalConfiguration config = UFTEncryptionGlobalConfiguration.getInstance();
                envs.put("hptoolslauncher.key", Secret.fromString(config.getEncKey()).getPlainText());
            } catch (NullPointerException ignored) {
                throw new IOException("Failed to access encryption key, the module UFTEncryption is unavailable.");
            }

            if (node == null) {
                node = JenkinsUtils.getCurrentNode(file);

                if (node == null) {
                    throw new IOException("Failed to access current executor node.");
                }
            }

            try {
                envs.put("hptoolslauncher.rootpath", Objects.requireNonNull(node.getRootPath()).getRemote());
            } catch (NullPointerException e) {
                throw new IOException(e.getMessage());
            }

            // Run the script on node
            // Execution result should be 0
            int returnCode = launcher.launch().cmds(args).stdout(out).pwd(file.getParent()).envs(envs).join();

            if (returnCode != 0) {
                if (returnCode == -1) {
                    build.setResult(Result.FAILURE);
                } else if (returnCode == -2) {
                    build.setResult(Result.UNSTABLE);
                } else if (returnCode == -3) {
                    build.setResult(Result.ABORTED);
                    // throwing this exception ensures we enter into the respective catch branch in the callstack
                    throw new InterruptedException();
                } else {
                    listener.getLogger().println("Launch return code " + returnCode);
                    build.setResult(Result.FAILURE);
                    if(returnCode == -2146232576 && file.getRemote().toLowerCase(Locale.ROOT).contains("\\system32\\config")) {
                        listener.getLogger().println("!!! Move 'JENKINS_HOME' out of the 'windows\\\\system32' folder because the plugin may not have permissions to launch tests under this folder.");
                    }
                }
            }
    }

    public static void runHpToolsAborterOnBuildEnv(
            AbstractBuild<?, ?> build,
            Launcher launcher,
            BuildListener listener,
            String paramFileName) throws IOException, InterruptedException {

            runHpToolsAborterOnBuildEnv(build, launcher, listener, paramFileName, build.getWorkspace());
    }

	@SuppressWarnings("squid:S2259")
	public static void runHpToolsAborterOnBuildEnv(
            Run<?, ?> build,
            Launcher launcher,
            TaskListener listener,
            String paramFileName, FilePath runWorkspace) throws IOException, InterruptedException {

        ArgumentListBuilder args = new ArgumentListBuilder();
        PrintStream out = listener.getLogger();

        String hpToolsAborter_exe = "HpToolsAborter.exe";

		URL hpToolsAborterUrl = Jenkins.get().pluginManager.uberClassLoader.getResource("HpToolsAborter.exe");
        FilePath hpToolsAborterFile = runWorkspace.child(hpToolsAborter_exe);
        
        args.add(hpToolsAborterFile);
        args.add(paramFileName);
        
        hpToolsAborterFile.copyFrom(hpToolsAborterUrl);
        
        int returnCode = launcher.launch().cmds(args).stdout(out).pwd(hpToolsAborterFile.getParent()).join();
        
        try {
        	hpToolsAborterFile.delete();
		} catch (Exception e) {
			 listener.error("failed copying HpToolsAborter" + e);
		}
        
        
        if (returnCode != 0) {
            if (returnCode == 1) {
                build.setResult(Result.FAILURE);
            } else if (returnCode == 2) {
                build.setResult(Result.UNSTABLE);
            } else if (returnCode == 3) {
                build.setResult(Result.ABORTED);
            }
        }
    }
    
    
}
