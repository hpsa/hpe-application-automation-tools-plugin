package com.hp.octane.plugins.jenkins.tests.detection;

import hudson.model.AbstractBuild;

import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * Service used for auto detection of test results global parameters like test-framework, testing-tool, etc.
 *
 */
public class ResultFieldsDetectionService {

    private static Logger logger = Logger.getLogger(ResultFieldsDetectionService.class.getName());

    public ResultFields getDetectedFields(AbstractBuild build) {
        for (ResultFieldsDetectionExtension ext : ResultFieldsDetectionExtension.all()) {
            try {
                ResultFields fields = ext.detect(build);
                if (fields != null) {
                    return fields;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during running of detection service: " + ext.getClass().getName(), e);
            }
        }
        return null;
    }
}
