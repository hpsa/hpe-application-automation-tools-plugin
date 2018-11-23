package com.microfocus.application.automation.tools.sse.common;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import java.io.File;

public class JsonHandlerMasterToSlave extends MasterToSlaveFileCallable<String> {
    @Override
    public String invoke(File f, VirtualChannel channel) {
        return JsonHandler.getStream(f);
    }
}
