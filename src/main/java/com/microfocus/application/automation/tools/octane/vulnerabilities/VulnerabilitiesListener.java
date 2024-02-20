/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.vulnerabilities;

import com.hp.octane.integrations.OctaneSDK;
import com.microfocus.application.automation.tools.octane.configuration.FodConfigUtil;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.configuration.SSCServerConfigUtil;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.listeners.RunListener;
import org.apache.logging.log4j.Logger;

/**
 * Jenkins events life cycle listener for processing vulnerabilities scan results on build completed
 */

@Extension
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872"})
public class VulnerabilitiesListener extends RunListener<AbstractBuild> {
    private static Logger logger = SDKBasedLoggerProvider.getLogger(VulnerabilitiesListener.class);

    @Override
    public void onFinalized(AbstractBuild build) {
        if (!OctaneSDK.hasClients()) {
            return;
        }

        SSCServerConfigUtil.SSCProjectVersionPair projectVersionPair = SSCServerConfigUtil.getProjectConfigurationFromBuild(build);
        if (!VulnerabilitiesUtils.insertQueueItem(build, projectVersionPair)) {
            return;
        }

        Long release = FodConfigUtil.getFODReleaseFromBuild(build);
        if (release != null) {
            logger.info("FOD configuration was found in " + build);
            VulnerabilitiesUtils.insertFODQueueItem(build, release);
        }
        if (projectVersionPair == null && release == null) {
            logger.debug("No Security Scan integration configuration was found " + build);
        }
    }
}
