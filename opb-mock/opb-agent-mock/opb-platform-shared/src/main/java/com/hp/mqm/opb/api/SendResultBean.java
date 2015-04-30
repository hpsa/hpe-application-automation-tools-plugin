package com.hp.mqm.opb.api;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: baruchg
 * Date: 11/09/13
 * Time: 21:57
 * This interface holds the result of information submission to MaaS
 */
public interface SendResultBean extends Serializable {
    /**
     * @return whether the last send request was successful or not
     */
    public boolean isSuccessful();

    /**
     * In case that the last send request was not successful due to the task is cancelled or in case that in the service side some exception was thrown
     * , this error code will be propagated to here
     * @return The error code of the exception thrown in the service side
     */
    public String getErrorCode();

    /**
     * In case that the last send request was not successful and in case that in the service side some exception was thrown with an error
     * message, this message will be propagated to here.
     * @return The error message of the exception thrown in the service side <br></br><b>note:</b> message length is trimmed to 512 characters at most according to com.hp.maas.platform.opb.controller.persistency.DBGeneralConsts.PersistencyFields.Request#STATUS_CONTENT_LENGTH
     */
    public String getErrorMessage();
}
