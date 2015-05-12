// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Item;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TestApi {

    private AbstractBuild build;

    public TestApi(AbstractBuild build) {
        this.build = build;
    }

    public void doAudit(StaplerRequest req, StaplerResponse res) throws IOException, ServletException, InterruptedException {
        // audit log contains possibly sensitive information (location, domain and project): require configure permission
        build.getProject().getACL().checkPermission(Item.CONFIGURE);
        serveFile(res, TestDispatcher.TEST_AUDIT_FILE, Flavor.JSON);
    }

    public void doXml(StaplerRequest req, StaplerResponse res) throws IOException, ServletException, InterruptedException {
        build.getACL().checkPermission(Item.READ);
        serveFile(res, TestListener.TEST_RESULT_FILE, Flavor.XML);
    }

    private void serveFile(StaplerResponse res, String relativePath, Flavor flavor) throws IOException, InterruptedException {
        res.setStatus(200);
        res.setContentType(flavor.contentType);
        FilePath file = new FilePath(new File(build.getRootDir(), relativePath));
        InputStream is = file.read();
        IOUtils.copy(is, res.getOutputStream());
        IOUtils.closeQuietly(is);
    }
}
