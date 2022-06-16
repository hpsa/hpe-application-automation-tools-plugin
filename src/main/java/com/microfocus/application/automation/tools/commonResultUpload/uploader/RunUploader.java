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

package com.microfocus.application.automation.tools.commonResultUpload.uploader;

import com.microfocus.application.automation.tools.commonResultUpload.CommonUploadLogger;
import com.microfocus.application.automation.tools.commonResultUpload.service.CustomizationService;
import com.microfocus.application.automation.tools.commonResultUpload.service.RestService;
import com.microfocus.application.automation.tools.commonResultUpload.service.RunStatusResolver;
import com.microfocus.application.automation.tools.results.service.almentities.AlmCommonProperties;
import com.microfocus.application.automation.tools.results.service.almentities.AlmRun;
import com.microfocus.application.automation.tools.results.service.almentities.AlmTestInstance;
import com.microfocus.application.automation.tools.results.service.almentities.IAlmConsts;
import org.apache.commons.lang.StringUtils;
import com.microfocus.application.automation.tools.sse.sdk.Base64Encoder;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microfocus.application.automation.tools.commonResultUpload.ParamConstant.ACTUAL_USER;

public class RunUploader {

    public static final String RUN_PREFIX = "runs";
    private static final String RUN_VERSION_MAP_NAME = "udf|Run On Version";
    private static final String VC_VERSION_NUMBER = "vc-version-number";

    private CommonUploadLogger logger;
    private Map<String, String> params;
    private Map<String, String> runStatusMapping;
    private RestService restService;
    private CustomizationService customizationService;

    public RunUploader(CommonUploadLogger logger, Map<String, String> params,
                       RestService restService, CustomizationService customizationService,
                       Map<String, String> runStatusMapping) {
        this.logger = logger;
        this.params = params;
        this.restService = restService;
        this.customizationService = customizationService;
        this.runStatusMapping = runStatusMapping;
    }

    public void upload(Map<String, String> testset, Map<String, String> test,
                       Map<String, String> testconfig, Map<String, String> testinstance,
                       Map<String, String> run) {
        // Set relations
        run.put(AlmRun.RUN_CONFIG_ID, testconfig.get(AlmCommonProperties.ID));
        run.put(AlmRun.RUN_CYCLE_ID, testset.get(AlmCommonProperties.ID));
        run.put(AlmRun.RUN_TEST_ID, test.get(AlmCommonProperties.ID));
        run.put(AlmRun.RUN_TESTCYCL_UNIQUE_ID, testinstance.get(AlmCommonProperties.ID));
        // Set calculated values
        run.put(AlmCommonProperties.OWNER, params.get(ACTUAL_USER));
        run.put(AlmCommonProperties.NAME, generateImportRunName());
        run.put(AlmRun.RUN_DURATION, convertDuration(run.get(AlmRun.RUN_DURATION)));
        run.put(AlmRun.RUN_SUBTYPE_ID,
                customizationService.getRunSubtypeIdByTestInstance(
                        testinstance.get(AlmTestInstance.TEST_INSTANCE_SUBTYPE_ID)));
        // Set values for external test
        if (StringUtils.isNotEmpty(run.get(AlmRun.RUN_DETAIL))) {
            run.put(AlmRun.RUN_DETAIL, convertDetail(run.get(AlmRun.RUN_DETAIL)));
        }

        if (!shouldProceedVersionForRun(test, run)) {
            return;
        }

        // Update test instance status
        String runstatus = RunStatusResolver.getRunStatus(run.get(AlmRun.RUN_STATUS), runStatusMapping);

        if (StringUtils.isNotEmpty(runstatus)) {
            // Create a run without status
            run.remove(AlmRun.RUN_STATUS);
            Map<String, String> createdRun = restService.create(RUN_PREFIX, run);

            // Update status of the run
            Map<String, String> updateRun = new HashMap<>();
            updateRun.put(AlmCommonProperties.ID, createdRun.get(AlmCommonProperties.ID));
            updateRun.put(AlmRun.RUN_STATUS, runstatus);

            // Retry update run status 3 times. For some ALM server may has limited DB connections then the update may fail.
            // Added here because only here uses RestService.update.
            // Otherwise the retry could be in RestService.update or UpdateAlmEntityEntityRequest.perform
            // depends on whether the result would be changed if update multiple times.
            // But this should be fixed at ALM server side to larger the connection number I think.

            Map<String, String> updateResult = restService.update(RUN_PREFIX, updateRun);
            if (updateResult == null) {
                for (int i = 0; i < 3; i++) {
                    if (updateResult == null) {
                        updateResult = restService.update(RUN_PREFIX, updateRun);
                    } else {
                        break;
                    }
                }
            }

        } else {
            restService.create(RUN_PREFIX, run);
        }
    }

    private String convertDetail(String detail) {
        if (StringUtils.isNotEmpty(detail)) {
            detail = detail.replaceAll("<", "&lt;");
            detail = detail.replaceAll(">", "&gt;");
            return Base64Encoder.encode(detail.getBytes());
        }
        return detail;
    }

    private String convertDuration(String duration) {
        if (duration != null && duration.length() > 0) {
            Float durationTime = 0.0f;
            try {
                durationTime = Float.valueOf(duration);
            } catch (NumberFormatException e) {
                return String.valueOf(0);
            }
            return String.valueOf(durationTime.intValue());
        } else {
            return String.valueOf(0);
        }
    }

    private String generateImportRunName() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new java.sql.Date(System.currentTimeMillis()));
        return String.format(
                IAlmConsts.IMPORT_RUN_NAME_TEMPLATE,
                // java.util.Calendar represents months from 0 to 11 instead of from 1 to 12.
                // That's why it should be incremented.
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
    }

    /**
     * Project without versioning, proceed
     * No "udf|run on version" in Run, proceed
     * No "vc-version-number" value in Test, proceed
     * Run version should be no bigger than latest test version
     * Set latest test version to run if run version has no value.
     * @param test
     * @param run
     * @return
     */
    private boolean shouldProceedVersionForRun(Map<String, String> test, Map<String, String> run) {
        // Some test type doesn't have version support
        for (String noVersionTest : TestUploader.NO_VERSION_TESTS) {
            if (test.get("subtype-id").equals(noVersionTest)) {
                return true;
            }
        }

        boolean shouldProceed = true;
        boolean versioningEnabled = customizationService.isVersioningEnabled(
                CustomizationService.TEST_ENTITY_NAME);
        if (versioningEnabled && run.containsKey(RUN_VERSION_MAP_NAME)
                && StringUtils.isNotEmpty(test.get(VC_VERSION_NUMBER))) {

            if (StringUtils.isEmpty(run.get(RUN_VERSION_MAP_NAME))) {
                run.put(RUN_VERSION_MAP_NAME, test.get(VC_VERSION_NUMBER));
                logger.info("Run on version not found. Set it as the latest test version.");
            } else {
                try {
                    int testLatestVersion = Integer.parseInt(test.get(VC_VERSION_NUMBER));
                    int runVersion = Integer.parseInt(run.get(RUN_VERSION_MAP_NAME));
                    if (runVersion > testLatestVersion) {
                        logger.error("Run version larger than test latest version.");
                        shouldProceed = false;
                    }
                    if (runVersion < 1) {
                        run.put(RUN_VERSION_MAP_NAME, "1");
                        logger.info("Run on version is " + runVersion + ". Minimum version should be 1. Set it to 1.");
                    }
                } catch (NumberFormatException e) {
                    logger.error("Version number illegal. " + e.getMessage());
                    shouldProceed = false;
                }
            }
        }
        return shouldProceed;
    }
}
