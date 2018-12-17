/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.octane.tests;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Item;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872", "squid:S2925", "squid:S109", "squid:S1607"})
public class TestApi {

	private AbstractBuild build;

	public TestApi(AbstractBuild build) {
		this.build = build;
	}

	public void doXml(StaplerRequest req, StaplerResponse res) throws IOException, InterruptedException {
		build.getACL().checkPermission(Item.READ);
		serveFile(res, TestListener.TEST_RESULT_FILE, Flavor.XML);
	}

	private void serveFile(StaplerResponse res, String relativePath, Flavor flavor) throws IOException, InterruptedException {
		FilePath file = new FilePath(new File(build.getRootDir(), relativePath));
		if (!file.exists()) {
			res.sendError(404, "Information not available");
			return;
		}
		res.setStatus(200);
		res.setContentType(flavor.contentType);
		InputStream is = file.read();
		IOUtils.copy(is, res.getOutputStream());
		IOUtils.closeQuietly(is);
	}
}
