/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.application.automation.tools.octane.configuration;

import com.hp.application.automation.tools.octane.Messages;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Item;
import org.kohsuke.stapler.StaplerProxy;

public class ConfigurationAction implements Action, StaplerProxy {

    final public AbstractProject owner;
    final public JobConfigurationProxy proxy;

    public ConfigurationAction(AbstractProject project) {
        this.owner = project;
        this.proxy = new JobConfigurationProxy(project);
    }

    @Override
    public String getIconFileName() {
        return owner.getACL().hasPermission(Item.CONFIGURE)? "setting.png": null;
    }

    @Override
    public String getDisplayName() {
        return Messages.ConfigurationLabel();
    }

    @Override
    public String getUrlName() {
        return "mqmConfiguration";
    }

    @Override
    public Object getTarget() {
        owner.getACL().checkPermission(Item.CONFIGURE);
        return this;
    }
}
