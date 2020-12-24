package com.microfocus.application.automation.tools.settings;

import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import hudson.XmlFile;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConfigurationMigrationUtil {

    private static final Logger logger = SDKBasedLoggerProvider.getLogger(ConfigurationMigrationUtil.class);

    private ConfigurationMigrationUtil(){
        //codacy : Add a private constructor to hide the implicit public one.
    }

    public static void migrateConfigurationFileIfRequired(XmlFile xmlFile, String oldFileName, String oldRootElementName, String newRootElementName) {
        if (!xmlFile.exists()) {
            //try to get from old path
            File oldXmlFile = new File(Jenkins.get().getRootDir(), oldFileName);
            if (oldXmlFile.exists()) {
                try {
                    String configuration = FileUtils.readFileToString(oldXmlFile, StandardCharsets.UTF_8.name());
                    String newConfiguration = StringUtils.replace(configuration,
                            oldRootElementName,
                            newRootElementName);
                    FileUtils.writeStringToFile(xmlFile.getFile(), newConfiguration, StandardCharsets.UTF_8.name());
                } catch (IOException e) {
                    logger.error("failed to migrate configuration to new 6.6 format  " + newRootElementName + " : " + e.getMessage());
                }
            }
        }
    }
}
