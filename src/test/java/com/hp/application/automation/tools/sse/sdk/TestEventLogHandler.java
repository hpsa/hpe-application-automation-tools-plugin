package com.hp.application.automation.tools.sse.sdk;

import java.net.HttpURLConnection;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.application.automation.tools.sse.common.TestCase;
import com.hp.application.automation.tools.sse.sdk.handler.EventLogHandler;

public class TestEventLogHandler implements TestCase {
    
    String _expectedEventLogUrl =
            "http://16.55.245.168:8081/qcbin/rest/domains/DEFAULT/projects/dani_demo/event-log-reads?query={context[\"*Timeslot:%20"
                    + TIMESLOT_ID
                    + "%3B*\"]}&fields=id,event-type,creation-time,action,description";
    
    @Test
    public void testLog() {
        
        Client client = new MockRestClient(URL, DOMAIN, PROJECT);
        EventLogHandler eventLogHandler = new EventLogHandler(client, TIMESLOT_ID);
        boolean isOk = eventLogHandler.log(new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
            }
        });
        Assert.assertTrue(isOk);
    }
    
    @Test
    public void testLogBadTimeslot() {
        
        Client client = new MockRestClientBadTimeslot(URL, DOMAIN, PROJECT);
        EventLogHandler eventLogHandler = new EventLogHandler(client, "");
        boolean isOk = eventLogHandler.log(new Logger() {
            
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        });
        Assert.assertTrue(isOk);
    }
    
    @Test
    public void testLogBadLog() {
        
        Client client = new MockRestClientBadLogResponse(URL, DOMAIN, PROJECT);
        EventLogHandler eventLogHandler = new EventLogHandler(client, TIMESLOT_ID);
        boolean isOk = eventLogHandler.log(new Logger() {
            
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        });
        Assert.assertFalse(isOk);
    }
    
    private class MockRestClient extends RestClient {
        
        public MockRestClient(String url, String domain, String project) {
            
            super(url, domain, project);
        }
        
        @Override
        public Response httpGet(String url, String queryString, Map<String, String> headers) {
            
            if (url.contains("event-log-reads")) {
                
                Assert.assertTrue(_expectedEventLogUrl.equals(url));
            }
            return new Response(
                    null,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Entities TotalResults=\"7\"><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1798</Value></Field><Field Name=\"description\"><Value>Timeslot ID '1005' was created successfully</Value></Field><Field Name=\"action\"><Value>Create Timeslot</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:42</Value></Field><Field Name=\"event-type\"><Value>Info</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1800</Value></Field><Field Name=\"description\"><Value>Creating run-time data for run '1036' of 'Test Set' '1' (Timeslot ID '1005'; BVS ID '1036')</Value></Field><Field Name=\"action\"><Value>Create run-time data</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:43</Value></Field><Field Name=\"event-type\"><Value>Info</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1801</Value></Field><Field Name=\"description\"><Value>TestSet ID: '1036' start time: '2013-02-19 12:03:43.907'</Value></Field><Field Name=\"action\"><Value>BVS Run</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:43</Value></Field><Field Name=\"event-type\"><Value>Info</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1802</Value></Field><Field Name=\"description\"><Value>Host 'vmltqa63' failed to run task '1244' of type 'hp.alm.test-execution'. Cause: The testing tool is not installed - ALM Lab service could not execute  test VAPI-XP because : ALM Lab service could not connect to the testing tool for Check host because Can't initialize host service process</Value></Field><Field Name=\"action\"><Value>Host Fail</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:50</Value></Field><Field Name=\"event-type\"><Value>Error</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1803</Value></Field><Field Name=\"description\"><Value>Host 'vmltqa63' ('1068') is about to become non operational and be replaced. The host is used by BVS run '1036' of BVS '1036' (timeslot '1005'). Reason: ALM Lab service could not execute  test VAPI-XP because : ALM Lab service could not connect to the testing tool for Check host because Can't initialize host service process</Value></Field><Field Name=\"action\"><Value>Host non operational</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:50</Value></Field><Field Name=\"event-type\"><Value>Error</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1805</Value></Field><Field Name=\"description\"><Value>Host 'vmltqa63' ('1068') became non operational and was replaced by host 'effi2' ('1065'). The host is assigned to timeslot '1005'</Value></Field><Field Name=\"action\"><Value>Host non operational</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:50</Value></Field><Field Name=\"event-type\"><Value>Error</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1808</Value></Field><Field Name=\"description\"><Value>Timeslot ID '1005' was closed</Value></Field><Field Name=\"action\"><Value>Close Timeslot</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:04:17</Value></Field><Field Name=\"event-type\"><Value>Info</Value></Field></Fields><RelatedEntities/></Entity></Entities>".getBytes(),
                    null,
                    HttpURLConnection.HTTP_OK);
        }
    }
    
    private class MockRestClientBadTimeslot extends RestClient {
        
        public MockRestClientBadTimeslot(String url, String domain, String project) {
            
            super(url, domain, project);
        }
        
        @Override
        public Response httpGet(String url, String queryString, Map<String, String> headers) {
            
            if (url.contains("event-log-reads")) {
                
                Assert.assertFalse(_expectedEventLogUrl.equals(url));
            }
            return new Response(
                    null,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Entities TotalResults=\"7\"><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1798</Value></Field><Field Name=\"description\"><Value>Timeslot ID '1005' was created successfully</Value></Field><Field Name=\"action\"><Value>Create Timeslot</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:42</Value></Field><Field Name=\"event-type\"><Value>Info</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1800</Value></Field><Field Name=\"description\"><Value>Creating run-time data for run '1036' of 'Test Set' '1' (Timeslot ID '1005'; BVS ID '1036')</Value></Field><Field Name=\"action\"><Value>Create run-time data</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:43</Value></Field><Field Name=\"event-type\"><Value>Info</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1801</Value></Field><Field Name=\"description\"><Value>TestSet ID: '1036' start time: '2013-02-19 12:03:43.907'</Value></Field><Field Name=\"action\"><Value>BVS Run</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:43</Value></Field><Field Name=\"event-type\"><Value>Info</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1802</Value></Field><Field Name=\"description\"><Value>Host 'vmltqa63' failed to run task '1244' of type 'hp.alm.test-execution'. Cause: The testing tool is not installed - ALM Lab service could not execute  test VAPI-XP because : ALM Lab service could not connect to the testing tool for Check host because Can't initialize host service process</Value></Field><Field Name=\"action\"><Value>Host Fail</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:50</Value></Field><Field Name=\"event-type\"><Value>Error</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1803</Value></Field><Field Name=\"description\"><Value>Host 'vmltqa63' ('1068') is about to become non operational and be replaced. The host is used by BVS run '1036' of BVS '1036' (timeslot '1005'). Reason: ALM Lab service could not execute  test VAPI-XP because : ALM Lab service could not connect to the testing tool for Check host because Can't initialize host service process</Value></Field><Field Name=\"action\"><Value>Host non operational</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:50</Value></Field><Field Name=\"event-type\"><Value>Error</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1805</Value></Field><Field Name=\"description\"><Value>Host 'vmltqa63' ('1068') became non operational and was replaced by host 'effi2' ('1065'). The host is assigned to timeslot '1005'</Value></Field><Field Name=\"action\"><Value>Host non operational</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:03:50</Value></Field><Field Name=\"event-type\"><Value>Error</Value></Field></Fields><RelatedEntities/></Entity><Entity Type=\"event-log-read\"><Fields><Field Name=\"id\"><Value>1808</Value></Field><Field Name=\"description\"><Value>Timeslot ID '1005' was closed</Value></Field><Field Name=\"action\"><Value>Close Timeslot</Value></Field><Field Name=\"creation-time\"><Value>2013-02-19 12:04:17</Value></Field><Field Name=\"event-type\"><Value>Info</Value></Field></Fields><RelatedEntities/></Entity></Entities>".getBytes(),
                    null,
                    HttpURLConnection.HTTP_OK);
        }
    }
    
    private class MockRestClientBadLogResponse extends RestClient {
        
        public MockRestClientBadLogResponse(String url, String domain, String project) {
            
            super(url, domain, project);
        }
        
        @Override
        public Response httpGet(String url, String queryString, Map<String, String> headers) {
            
            return new Response(null, "".getBytes(), null, HttpURLConnection.HTTP_OK);
        }
    }
}
