package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.ExecutorException;
import com.hp.mqm.opb.FutureSendResult;
import com.hp.mqm.opb.api.SendResultBean;

/**
 * Created with IntelliJ IDEA.
 * User: halilio
 * Date: 2/10/14
 * Time: 2:54 PM
 * This class if a mock for future sent result
 */
public class FutureSendResultMockImpl implements FutureSendResult {

    private SendResultBean resultWithMillis = new SendResultBeanImpl("id1", "code1", "message1");
    private SendResultBean result = new SendResultBeanImpl("id2", "code2", "message2");
    private SendResultBean tryGetResult = new SendResultBeanImpl("id3", "code3", "message3");

    @Override
    public SendResultBean getResult() throws ExecutorException {
        return result;
    }

    @Override
    public SendResultBean getResult(int timeoutMillis) throws ExecutorException {
        return resultWithMillis;
    }

    @Override
    public SendResultBean tryGetResult() throws ExecutorException {
        return tryGetResult;
    }

    private class SendResultBeanImpl implements SendResultBean {

        private String requestId;
        private String errorCode;
        private String errorMessage;

        public SendResultBeanImpl(String requestId, String errorCode, String errorMessage) {
            this.requestId = requestId;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        @Override
        public boolean isSuccessful() {
            return false;
        }

        @Override
        public String getErrorCode() {
            return errorCode;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
