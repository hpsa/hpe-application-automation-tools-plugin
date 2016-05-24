package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.hpe.application.automation.tools.common.integration.HttpConnectionException;
import com.hpe.application.automation.tools.common.integration.JobOperation;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.String;
import java.util.*;
import java.io.*;


public class UploadApplicationTask implements TaskType {

    private final TestCollationService testCollationService;

    public UploadApplicationTask(@NotNull final TestCollationService testCollationService) {
        this.testCollationService = testCollationService;
    }

    @NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {

        final ConfigurationMap map = taskContext.getConfigurationMap();
        final BuildLogger buildLogger = taskContext.getBuildLogger();


        String mcServerUrl = map.get(UploadApplicationTaskConfigurator.MCSERVERURL);
        String mcUserName = map.get(UploadApplicationTaskConfigurator.MCUSERNAME);
        String mcPassword = map.get(UploadApplicationTaskConfigurator.MCPASSWORD);

        //proxy info
        String proxyAddress = null;
        String proxyUserName = null;
        String proxyPassword = null;

        boolean useProxy = BooleanUtils.toBoolean(map.get(RunFromFileSystemTaskConfigurator.USE_PROXY));

        if (useProxy) {

            proxyAddress = map.get(RunFromFileSystemTaskConfigurator.PROXY_ADDRESS);

            Boolean specifyAutheration = BooleanUtils.toBoolean(RunFromFileSystemTaskConfigurator.SPECIFY_AUTHERATION);

            if (specifyAutheration) {
                proxyUserName = map.get(RunFromFileSystemTaskConfigurator.PROXY_USERNAME);
                proxyPassword = map.get(RunFromFileSystemTaskConfigurator.PROXY_PASSWORD);
            }
        }


        JobOperation operation = new JobOperation(mcServerUrl, mcUserName, mcPassword, proxyAddress, proxyUserName, proxyPassword);

        List<String> lst = UploadApplicationTaskConfigurator.fetchMCApplicationPathFromContext(map);

        for (String path : lst) {

            String appName = getAppName(path);

            buildLogger.addBuildLogEntry("********** Start Upload App :" + appName);

            try {
                String info = operation.upload(path);
            } catch (HttpConnectionException e) {
                buildLogger.addErrorLogEntry("********** Fail to connect Mobile Center Server, Please check the URL, UserName, Password, and Proxy configuration **********");
                return TaskResultBuilder.create(taskContext).failedWithError().build();
            } catch (FileNotFoundException e) {
                buildLogger.addErrorLogEntry("********** Fail to find the App path you provided, please check it **********");
                return TaskResultBuilder.create(taskContext).failedWithError().build();
            } catch (IOException e) {
                buildLogger.addErrorLogEntry("********** Some problems appeared when upload app **********");
                return TaskResultBuilder.create(taskContext).failedWithError().build();
            }

            buildLogger.addBuildLogEntry("********** The App Upload Successfully **********");
        }

        if(lst.size() > 1){
            buildLogger.addBuildLogEntry("********** All Apps Upload Successfully **********");
        }

        return collateResults(taskContext);
    }

    private String getAppName(String path){
        File file = new File(path);
        return file.getName();
    }


    private TaskResult collateResults(@NotNull final TaskContext taskContext) {
        try {
            TestResultHelper.CollateResults(testCollationService, taskContext);

            return TaskResultBuilder.create(taskContext).build();
        } catch (Exception ex) {
            return TaskResultBuilder.create(taskContext).failed().build();
        }
    }

}