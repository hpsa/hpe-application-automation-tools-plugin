package com.hp.mqm.opb.api;

/**
 * Created with IntelliJ IDEA.
 * User: baruchg
 * Date: 06/11/13
 * Time: 14:48
 */
public interface TaskInputDataResult extends TaskInputData{
    /**
     * @return whether the last receive request was successful or not
     */
    public boolean isSuccessful();

    /**
     * In case that the last receive request was not successful and in case that in the service side some exception was thrown with an error
     * message, this message will be propagated to here.
     * @return The error message of the exception thrown in the service side <br></br><b>note:</b> message length is trimmed to 512 characters at most according to com.hp.maas.platform.opb.controller.persistency.DBGeneralConsts.PersistencyFields.Request#STATUS_CONTENT_LENGTH
     */
    public String getErrorMessage();
}
