// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hpe.application.automation.tools.octane.tests.junit;

import hudson.FilePath;

import java.io.IOException;
import java.io.Serializable;

public interface ModuleDetection extends Serializable {

    String getModule(FilePath resultFile) throws IOException, InterruptedException;

    class Default implements ModuleDetection {

        @Override
        public String getModule(FilePath resultFile) throws IOException, InterruptedException {
            return "";
        }
    }
}
