// (c) Copyright 2016 Hewlett Packard Enterprise Development LP
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.run;

import java.io.PrintStream;

import com.hp.application.automation.tools.model.SvServiceSelectionModel;
import com.hp.application.automation.tools.model.SvUndeployModel;
import com.hp.sv.jsvconfigurator.processor.IUndeployProcessor;
import com.hp.sv.jsvconfigurator.processor.UndeployProcessor;
import com.hp.sv.jsvconfigurator.processor.UndeployProcessorInput;
import com.hp.sv.jsvconfigurator.serverclient.ICommandExecutor;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.kohsuke.stapler.DataBoundConstructor;

public class SvUndeployBuilder extends AbstractSvRunBuilder<SvUndeployModel> {

    @DataBoundConstructor
    public SvUndeployBuilder(String serverName, boolean force, SvServiceSelectionModel serviceSelection) {
        super(new SvUndeployModel(serverName, force, serviceSelection));
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public boolean performImpl(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws Exception {
        PrintStream logger = listener.getLogger();

        IUndeployProcessor processor = new UndeployProcessor(null);

        ICommandExecutor exec = createCommandExecutor();
        for (ServiceInfo service : getServiceList()) {
            logger.printf("  Undeploying service '%s' [%s] %n", service.getName(), service.getId());
            UndeployProcessorInput undeployProcessorInput = new UndeployProcessorInput(model.isForce(), null, service.getId());
            processor.process(undeployProcessorInput, exec);
        }

        return true;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractSvRunDescriptor {

        public DescriptorImpl() {
            super("SV: Undeploy Virtual Service");
        }
    }
}
