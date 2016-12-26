package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;

/**
 * Created by berkovir on 29/06/2016.
 */
public class Run extends AlmEntity {

    public static String TYPE = "run";
    public static String COLLECTION_NAME = "runs";

    public static String FIELD_DATE = "execution-date";
    public static String FIELD_TIME = "execution-time";
    public static String FIELD_DURATION = "duration";
    public static String FIELD_STATUS = "status";
    public static String FIELD_TYPE = "subtype-id";
    public static String FIELD_TEST_ID = "test-id";
    public static String FIELD_TEST_SET_ID = "cycle-id";
    public static String FIELD_TEST_INSTANCE_ID = "testcycl-id";
    public static String FIELD_OS_NAME = "os-name";
    public static String FIELD_SPRINT_ID = "assign-rcyc";
    public static String FIELD_DRAFT = "draft";
    public static String FIELD_EXECUTOR = "owner";
    public static String FIELD_TEST_CONFIG_ID = "test-config-id";

    public Run() {
        super(TYPE);
    }

    public String getTestSetId() {
        return getString(FIELD_TEST_SET_ID);
    }

    public String getSprintId() {
        return this.getString(FIELD_SPRINT_ID);
    }

    public String getTestConfigId() {
        return getString(FIELD_TEST_CONFIG_ID);
    }


    public String getTestId() {
        return getString(FIELD_TEST_ID);
    }

    public String getDuration() {
        return getString(FIELD_DURATION);
    }

    public String getOsName() {
        return getString(FIELD_OS_NAME);
    }

    public String getExecutionDate() {
        return getString(FIELD_DATE);
    }

    public String getExecutionTime() {
        return getString(FIELD_TIME);
    }

    public String getStatus() {
        return getString(FIELD_STATUS);
    }

    public String getExecutor() {
        return getString(FIELD_EXECUTOR);
    }

    public String getDraft() {
        return getString(FIELD_DRAFT);
    }
}
