package com.hp.mqm.opb.loopback.mock.agent;


import com.hp.mqm.opb.api.TaskInputDataResult;

/**
 * Created with IntelliJ IDEA.
 * User: ginni
 * Date: 4/15/15
 * mock to task input data
 */
public class TaskInputDataMockImpl implements TaskInputDataResult {
    private static final long serialVersionUID = -2216200389201450231L;
    private final byte[] data;
    private final String errorMessage;
    private final boolean isSuccessful;

    public TaskInputDataMockImpl(byte[] data) {
        this(data, "", true);
    }

    public TaskInputDataMockImpl(String errorMessage) {
        this(null, errorMessage, false);
    }

    private TaskInputDataMockImpl(byte[] data, String errorMessage, boolean successful) {
        this.data = data;
        this.errorMessage = errorMessage;
        isSuccessful = successful;
    }

    @Override
    public boolean isSuccessful() {
        return isSuccessful;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
