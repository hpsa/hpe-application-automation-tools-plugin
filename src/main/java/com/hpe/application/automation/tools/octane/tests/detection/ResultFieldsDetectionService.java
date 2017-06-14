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

package com.hpe.application.automation.tools.octane.tests.detection;

import hudson.model.Run;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service used for auto detection of test results global parameters like test-framework, testing-tool, etc.
 */
public class ResultFieldsDetectionService {
	private static Logger logger = LogManager.getLogger(ResultFieldsDetectionService.class);

	public ResultFields getDetectedFields(Run<?,?> build) throws InterruptedException {
		for (ResultFieldsDetectionExtension ext : ResultFieldsDetectionExtension.all()) {
			try {
				ResultFields fields = ext.detect(build);
				if (fields != null) {
					return fields;
				}
			} catch (InterruptedException e) {
				logger.error("Interrupted during running of detection service: " + ext.getClass().getName(), e);
				throw e;
			} catch (Exception e) {
				logger.error("Error during running of detection service: " + ext.getClass().getName(), e);
			}
		}
		return null;
	}
}
