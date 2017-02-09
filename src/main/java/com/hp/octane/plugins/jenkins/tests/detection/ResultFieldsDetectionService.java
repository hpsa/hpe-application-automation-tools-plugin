package com.hp.octane.plugins.jenkins.tests.detection;

import hudson.model.AbstractBuild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service used for auto detection of test results global parameters like test-framework, testing-tool, etc.
 */
public class ResultFieldsDetectionService {
	private static Logger logger = LogManager.getLogger(ResultFieldsDetectionService.class);

	public ResultFields getDetectedFields(AbstractBuild build) throws InterruptedException {
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
