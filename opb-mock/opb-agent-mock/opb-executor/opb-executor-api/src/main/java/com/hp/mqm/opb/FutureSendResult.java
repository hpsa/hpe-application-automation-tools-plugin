package com.hp.mqm.opb;

import com.hp.mqm.opb.api.SendResultBean;
import com.hp.mqm.opb.api.TaskCancelledException;

/**
 * Created with IntelliJ IDEA.
 * User: baruchg
 * Date: 11/09/13
 * Time: 21:16
 * This interface is the result of a
 * sendData(java.lang.String, com.hp.mqm.opb.api.TaskOutputData)}
 * it can be used in order to verify that the send data executed during the implementation of a task execution was successfully sent
 * and act accordingly.
 */
public interface FutureSendResult {
    /**
     * @return SendResultBean which holds information about the send status of the send request referenced by this FutureSendResult instance
     * waiting for result to arrive maximum allowed time
     * <br></br><b>note:</b> if timeout was triggered then result is <code>null</code>, when using this function check for nullity first.
     * <br></br><b>note:</b> this method is blocking current thread, use with care
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     * */
    public SendResultBean getResult() throws ExecutorException, TaskCancelledException;

    /**
     * @param timeoutMillis timeout in milliseconds to block current thread before return, see note below regarding return value
     * @return SendResultBean which holds information about the send status of the send request referenced by this FutureSendResult instance
     * <br></br><b>note:</b> if timeout was triggered then result is <code>null</code>, when using this function check for nullity first.
     * <br></br><b>note:</b> this method is blocking current thread, use with care
     * @throws IllegalArgumentException is thrown if timeoutMillis < 1
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public SendResultBean getResult(int timeoutMillis) throws ExecutorException, TaskCancelledException;


    /**
     * @return SendResultBean which holds information about the send status of the send request referenced by this FutureSendResult instance
     * <br></br><b>note:</b> this method is not blocking and thus, will return <code>null</code> if the send result is not yet known
     *
     * @throws  TaskCancelledException will be thrown in case the current task is cancelled
     *
     */
    public SendResultBean tryGetResult() throws ExecutorException, TaskCancelledException;
}
