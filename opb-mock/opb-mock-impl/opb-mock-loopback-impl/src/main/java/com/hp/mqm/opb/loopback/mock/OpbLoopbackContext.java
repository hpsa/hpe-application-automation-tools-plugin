package com.hp.mqm.opb.loopback.mock;

import com.hp.mqm.opb.api.EndpointCredentials;
import com.hp.mqm.opb.loopback.mock.agent.ExecutorAPIMockImpl;
import com.hp.mqm.opb.loopback.mock.service.OpbServiceMockImpl;
import com.hp.mqm.opb.service.api.OpbServiceApi;
import com.hp.mqm.opb.service.api.entities.OpbEndpoint;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class OpbLoopbackContext {


    private Map<Integer, OpbEndpoint> endPointMap;

    private static final long DEFAULT_DELAY_MILLIS = 1000;
    private long executeTaskDelayIntervalMillis = DEFAULT_DELAY_MILLIS;
    private long incomingDataDelayIntervalMillis = DEFAULT_DELAY_MILLIS;
    private long outgoingDataDelayIntervalMillis = DEFAULT_DELAY_MILLIS;

    private Map<String, EndpointCredentials> credentials;
    private OpbServiceMockImpl opbService;

    public static OpbLoopbackContext initContext() {
        return new OpbLoopbackContext();
    }

    private OpbLoopbackContext() {
        credentials = new HashMap<>();
        endPointMap = new HashMap<>();
    }

    /**
     * adds an end point
     * @param endPoint the execution endpoint
     */
    public void addEndPoint(OpbEndpoint endPoint) {
        endPointMap.put(endPoint.getId(), endPoint);
    }

    /**
     * make the callbacks work more like in reality by adding delay
     * 1. To simulate network operation
     * 2. To avoid calling all the callback at once. In the real implementation to order is preserved.
     */
    public void setExecuteTaskDelayIntervalMillis(long executeTaskDelayIntervalMillis) {
        this.executeTaskDelayIntervalMillis = executeTaskDelayIntervalMillis;
    }
    /**
     * make the callbacks asynchronous (more like in reality)
     */
    public void setIncomingDataDelayIntervalMillis(long incomingDataDelayIntervalMillis) {
        this.incomingDataDelayIntervalMillis = incomingDataDelayIntervalMillis;
    }

    /**
     *  make the callbacks asynchronous (more like in reality)
     */
    public void setOutgoingDataDelayIntervalMillis(long outgoingDataDelayIntervalMillis) {
        this.outgoingDataDelayIntervalMillis = outgoingDataDelayIntervalMillis;
    }

    public OpbServiceApi getOpbService() {
        ExecutorAPIMockImpl executorAPI = new ExecutorAPIMockImpl();
        opbService = new OpbServiceMockImpl(executorAPI,
                endPointMap, executeTaskDelayIntervalMillis, incomingDataDelayIntervalMillis, outgoingDataDelayIntervalMillis);
        executorAPI.setIntegrationService(opbService);
        executorAPI.setCredentials(credentials);
        return opbService;
    }

    public void registerCredentials(final String id, final String name, final String user, final String password, final HashMap<String, Object> properties) {
        this.credentials.put(name, new EndpointCredentials() {
            public String getId() { return id;}
            public String getName() {return name;}
            public String getUser() {return user;}
            public byte[] getPassword() {return password.getBytes(Charset.forName("UTF-8"));}
            public HashMap<String, Object> getParameters() {return properties;}
        });
    }

    /**
     * This method waits for all the threads that were run by the mocks.
     */
    public void joinAllThreads() {
        if (opbService != null) {
            opbService.joinAllThreads();
        }
    }
}

