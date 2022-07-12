/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
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