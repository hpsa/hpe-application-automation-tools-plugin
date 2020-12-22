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

package com.microfocus.application.automation.tools.octane.actions.coverage;

import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.FilePath;
import hudson.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * this action initiate a copy of all coverage reports from workspace to build folder.
 * the files are calculated by a pattern that the user enters in job configuration page
 */
public class CoveragePublisherAction implements Action {
	private final Run build;

	public CoveragePublisherAction(Run build, TaskListener listener) {
		this.build = build;
		CoverageService.setListener(listener);
	}

	/**
	 * this method copy all reports from specified path pattern
	 *
	 * @return list of file names that were copied; NEVER NULL; if an empty list returned - no coverage reports found
	 */
	public List<String> copyCoverageReportsToBuildFolder(String filePattern, String defaultFileName) {
		List<String> result = new LinkedList<>();
		FilePath workspace = BuildHandlerUtils.getWorkspace(build);
		if (workspace != null) {
			try {
				CoverageService.log("start copying coverage report to build folder, using file patten of " + filePattern);
				String[] files = CoverageService.getCoverageFiles(workspace, filePattern);
				List<String> matchingReportFiles = filterFilesByFileExtension(files);
				int index = 0;

				for (String fileName : matchingReportFiles) {
					File resultFile = new File(workspace.child(fileName).toURI());
					String nextOutputFilename = CoverageService.getCoverageReportFileName(index++, defaultFileName);
					result.add(nextOutputFilename);
					File targetReportFile = new File(build.getRootDir(), nextOutputFilename);
					CoverageService.copyCoverageFile(resultFile, targetReportFile, workspace);
				}

				if (result.isEmpty()) {
					// most likely a configuration error in the job - e.g. false pattern to match the cucumber result files
					CoverageService.log("No coverage file that matched the specified pattern was found in workspace");
				}
			} catch (Exception e) {
				CoverageService.log("Copying coverage files to build folder failed because of " + e.toString());
			}
		}
		return result;
	}

	/**
	 * pre validation of coverage files by file extension.
	 *
	 * @param files to validate
	 * @return filtered list of files
	 */
	private ArrayList<String> filterFilesByFileExtension(String[] files) {
		ArrayList<String> filteredList = new ArrayList<>();
		for (String fileFullPath : files) {
			if (fileFullPath.endsWith(CoverageService.Lcov.LCOV_FILE_EXTENSION) || fileFullPath.endsWith(CoverageService.Jacoco.JACOCO_FILE_EXTENSION)) {
				filteredList.add(fileFullPath);
			}
		}
		return filteredList;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return null;
	}
}
