package com.hp.mqm.opb;

/**
 * This enum represents the various task execution finish statuses.
 *
 * Any modification should be reflected as well in com.hp.maas.platform.services.opb.api.TaskFinishStatus
 *
 * User: ginni
 * Date: 4/15/15
 * 
 */
public enum TaskFinishStatus {
    FAILED, SUCCESS, FINISHED_WITH_ERRORS
}
