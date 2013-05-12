package com.hp.application.automation.tools.sse.sdk;

import java.net.HttpURLConnection;
import java.util.Map;

import com.hp.application.automation.tools.sse.common.TestCase;

public class MockRestClientStop extends RestClient implements TestCase {
    
    private Object _lock;
    
    public MockRestClientStop(String url, String domain, String project) {
        
        super(url, domain, project);
    }
    
    @Override
    public Response httpGet(String url, String queryString, Map<String, String> headers) {
        
        Response ret = new Response();
        if (url.contains("rest/is-authenticated")) {
            ret = new Response(null, null, null, HttpURLConnection.HTTP_OK);
        } else if (url.contains("procedure-runs/")) {
            ret = new Response(null, RUN_ENTITY_DATA_FORMAT, null, HttpURLConnection.HTTP_OK);
        } else if (url.contains("event-log-reads")) {
            ret = new Response(null, EVENT_LOG_DATA, null, HttpURLConnection.HTTP_OK);
        } else if (url.contains("procedure-testset-instance-runs")) {
            ret.setData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Entities TotalResults=\"1\"><Entity Type=\"procedure-testset-instance-run\"><Fields><Field Name=\"run-id\"><Value>7</Value></Field><Field Name=\"location\"><Value></Value></Field><Field Name=\"state\"><Value>Finished</Value></Field><Field Name=\"purpose\"><Value>VAPI-XP</Value></Field><Field Name=\"on-failure-settings\"><Value></Value></Field><Field Name=\"exec-progress-details\"><Value>Passed</Value></Field><Field Name=\"id\"><Value>1008</Value></Field><Field Name=\"start-exec-date\"><Value>2013-02-13</Value></Field><Field Name=\"ver-stamp\"/><Field Name=\"host-name\"><Value>Any</Value></Field><Field Name=\"order\"><Value>1</Value></Field><Field Name=\"test-config-name\"><Value>vapi1</Value></Field><Field Name=\"testset-name\"><Value>testset1</Value></Field><Field Name=\"testcycl-id\"><Value>1</Value></Field><Field Name=\"host\"><Value>schreida1</Value></Field><Field Name=\"status\"><Value>Passed</Value></Field><Field Name=\"testing-host\"><Value>Host with purpose(s): VAPI-XP</Value></Field><Field Name=\"host-demand\"><Value></Value></Field><Field Name=\"test-subtype\"><Value>hp.qc.test-instance.VAPI-XP-TEST</Value></Field><Field Name=\"test-name\"><Value>vapi1</Value></Field><Field Name=\"vts\"/><Field Name=\"test-run-try\"><Value></Value></Field><Field Name=\"start-exec-time\"><Value>14:32:28</Value></Field><Field Name=\"parent-id\"><Value>1008</Value></Field><Field Name=\"procedure-run\"><Value>1008</Value></Field><Field Name=\"duration\"><Value>0</Value></Field><Field Name=\"attributes\"><Value></Value></Field><Field Name=\"exec-progress\"><Value>Finished</Value></Field></Fields><RelatedEntities/></Entity></Entities>".getBytes());
        } else if (url.contains("test-sets/")) {
            ret.setData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Entity Type=\"test-set\"><Fields><Field Name=\"close-date\"/><Field Name=\"request-id\"/><Field Name=\"mail-settings\"><Value></Value></Field><Field Name=\"pc-is-valid\"><Value>Y</Value></Field><Field Name=\"id\"><Value>1</Value></Field><Field Name=\"os-config\"><Value></Value></Field><Field Name=\"pc-validation-result-xml\"><Value>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;reasons/&gt;</Value></Field><Field Name=\"description\"><Value></Value></Field><Field Name=\"name\"><Value>testset1</Value></Field><Field Name=\"has-linkage\"><Value>N</Value></Field><Field Name=\"cycle-ver-stamp\"><Value>2</Value></Field><Field Name=\"pinned-baseline\"><Value></Value></Field><Field Name=\"report-settings\"><Value></Value></Field><Field Name=\"last-modified\"><Value>2013-02-13 12:47:11</Value></Field><Field Name=\"assign-rcyc\"><Value></Value></Field><Field Name=\"status\"><Value>Open</Value></Field><Field Name=\"cycle-config\"><Value></Value></Field><Field Name=\"exec-event-handle\"><Value></Value></Field><Field Name=\"open-date\"><Value>2013-02-13</Value></Field><Field Name=\"attachment\"><Value></Value></Field><Field Name=\"subtype-id\"><Value>hp.sse.test-set.process</Value></Field><Field Name=\"pc-is-demand-customized\"><Value></Value></Field><Field Name=\"parent-id\"><Value>1</Value></Field><Field Name=\"comment\"><Value></Value></Field></Fields><RelatedEntities/></Entity>".getBytes());
        } else if (url.contains("procedures/")) {
            ret.setData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Entity Type=\"Procedure\"><Fields><Field Name=\"is-valid\"><Value>Y</Value></Field><Field Name=\"creation-date\"><Value>2013-02-13</Value></Field><Field Name=\"created-by\"><Value>sa</Value></Field><Field Name=\"modified-by\"><Value>sa</Value></Field><Field Name=\"vts\"><Value>2013-02-13 14:49:51</Value></Field><Field Name=\"id\"><Value>1010</Value></Field><Field Name=\"parent-id\"><Value>1</Value></Field><Field Name=\"ver-stamp\"><Value>12</Value></Field><Field Name=\"is-autogenerated\"><Value>N</Value></Field><Field Name=\"description\"><Value></Value></Field><Field Name=\"name\"><Value>bvs1</Value></Field><Field Name=\"is-demand-customized\"><Value>N</Value></Field><Field Name=\"validation-result-xml\"><Value>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;reasons/&gt;</Value></Field></Fields><RelatedEntities/></Entity>".getBytes());
        } else if (url.contains("reservations/")) {
            ret = new Response(null, RUNNING_DATA, null, HttpURLConnection.HTTP_OK);
        }
        ret.setStatusCode(200);
        
        return ret;
    }
    
    @Override
    public Response httpPost(String url, byte[] data, Map<String, String> headers) {
        
        Response ret = new Response();
        if (url.contains("startrun")) {
            ret.setData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Entity Type=\"StartRunTestSetResponse\"><Fields><Field Name=\"SuccessStaus\"><Value>1</Value></Field><Field Name=\"info\"><Value>1005</Value></Field></Fields></Entity>".getBytes());
        }
        ret.setStatusCode(201);
        synchronized (_lock) {
            _lock.notifyAll();
        }
        
        return ret;
    }
    
    public void setLock(Object lock) {
        
        _lock = lock;
    }
}
