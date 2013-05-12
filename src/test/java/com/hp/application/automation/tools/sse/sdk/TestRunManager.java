package com.hp.application.automation.tools.sse.sdk;

import org.junit.Assert;
import org.junit.Test;

import com.hp.application.automation.tools.model.CdaDetails;
import com.hp.application.automation.tools.model.SseModel;
import com.hp.application.automation.tools.sse.ArgsFactory;
import com.hp.application.automation.tools.sse.common.TestCase;
import com.hp.application.automation.tools.sse.result.model.junit.Testcase;
import com.hp.application.automation.tools.sse.result.model.junit.Testsuites;

public class TestRunManager implements TestCase {
    
    @Test
    public void testEndToEndBVS() throws InterruptedException {
        
        SseModel model = createBvsModel();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientFunctional(args.getUrl(), args.getDomain(), args.getProject());
        Testsuites testsuites = new RunManager().execute(connection, args, new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
                
            }
        });
        
        Assert.assertNotNull(testsuites);
        Assert.assertTrue(verifyTestsuitesFunctional(testsuites, "bvs"));
    }
    
    @Test
    public void testEndToEndBVSWithCDA() throws InterruptedException {
        
        SseModel model = createBvsModelWithCDA();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientFunctional(args.getUrl(), args.getDomain(), args.getProject());
        Testsuites testsuites = new RunManager().execute(connection, args, new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
                
            }
        });
        Assert.assertNotNull(testsuites);
        Assert.assertTrue(verifyTestsuitesFunctional(testsuites, "bvs"));
    }
    
    @Test
    public void testEndToEndBVSWithEmptyCDA() throws InterruptedException {
        
        SseModel model = createBvsModelWithEmptyCDA();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientFunctional(args.getUrl(), args.getDomain(), args.getProject());
        Testsuites testsuites = new RunManager().execute(connection, args, new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
                
            }
        });
        Assert.assertNotNull(testsuites);
        Assert.assertTrue(verifyTestsuitesFunctional(testsuites, "bvs"));
    }
    
    @Test
    public void testEndToEndTestSet() throws InterruptedException {
        
        SseModel model = createTestSetModel();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientFunctional(args.getUrl(), args.getDomain(), args.getProject());
        Testsuites testsuites = new RunManager().execute(connection, args, new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
                
            }
        });
        
        Assert.assertNotNull(testsuites);
        Assert.assertTrue(verifyTestsuitesFunctional(testsuites, "testset"));
    }
    
    @Test
    public void testEndToEndPC() throws InterruptedException {
        
        SseModel model = createPCModel();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientPC(args.getUrl(), args.getDomain(), args.getProject());
        Testsuites testsuites = new RunManager().execute(connection, args, new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
                
            }
        });
        
        Assert.assertNotNull(testsuites);
        Assert.assertTrue(verifyTestsuitesPC(testsuites, "testset"));
    }
    
    @Test
    public void testBadRunEntity() throws InterruptedException {
        
        SseModel model = createBvsModel();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientBadRunEntity(args.getUrl(), args.getDomain(), args.getProject());
        RunManager runManager = new RunManager();
        Testsuites testsuites = runManager.execute(connection, args, new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
                
            }
        });
        
        Assert.assertNull(testsuites);
        
    }
    
    @Test
    public void testLoginFailed() throws InterruptedException {
        
        SseModel model = createBvsModel();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientFailedLogin(args.getUrl(), args.getDomain(), args.getProject());
        Testsuites testsuites = new RunManager().execute(connection, args, new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
                
            }
        });
        
        Assert.assertNull(testsuites);
    }
    
    @Test
    public void testBadDomain() throws InterruptedException {
        
        SseModel model = createBvsModel();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientFailedLogin(args.getUrl(), args.getDomain(), args.getProject());
        Testsuites testsuites = new RunManager().execute(connection, args, new Logger() {
            
            @Override
            public void log(String message) {
                
                System.out.println(message);
                
            }
        });
        
        Assert.assertNull(testsuites);
    }
    
    @Test
    public void testBadRunResponse() {
        
        SseModel model = createTestSetModel();
        model.setAlmServerUrl(URL);
        Args args = new ArgsFactory().create(model);
        RestClient connection =
                new MockRestClientBadRunResponse(args.getUrl(), args.getDomain(), args.getProject());
        boolean thrown = false;
        try {
            new RunManager().execute(connection, args, new Logger() {
                
                @Override
                public void log(String message) {
                    
                    System.out.println(message);
                    
                }
            });
        } catch (Throwable cause) {
            thrown = true;
        }
        
        Assert.assertTrue(thrown);
    }
    
    private MockSseModel createBvsModel() {
        
        return new MockSseModel(
                SERVERNAME,
                USER,
                PASS,
                DOMAIN,
                PROJECT,
                "BVS",
                "1041",
                DURATION,
                DESCRIPTION,
                POST_RUN_ACTION,
                ENVIRONMENT_CONFIGURATION_ID,
                null);
    }
    
    private MockSseModel createBvsModelWithCDA() {
        
        return new MockSseModel(
                SERVERNAME,
                USER,
                PASS,
                DOMAIN,
                PROJECT,
                "BVS",
                "1041",
                DURATION,
                DESCRIPTION,
                POST_RUN_ACTION,
                ENVIRONMENT_CONFIGURATION_ID,
                CDA_DETAILS);
    }
    
    private MockSseModel createBvsModelWithEmptyCDA() {
        
        return new MockSseModel(
                SERVERNAME,
                USER,
                PASS,
                DOMAIN,
                PROJECT,
                "BVS",
                "1041",
                DURATION,
                DESCRIPTION,
                POST_RUN_ACTION,
                ENVIRONMENT_CONFIGURATION_ID,
                new CdaDetails("", "", ""));
    }
    
    private MockSseModel createTestSetModel() {
        
        return new MockSseModel(
                SERVERNAME,
                USER,
                PASS,
                DOMAIN,
                PROJECT,
                "TEST_SET",
                "1041",
                DURATION,
                DESCRIPTION,
                POST_RUN_ACTION,
                ENVIRONMENT_CONFIGURATION_ID,
                null);
    }
    
    private MockSseModel createPCModel() {
        
        return new MockSseModel(
                SERVERNAME,
                USER,
                PASS,
                DOMAIN,
                PROJECT,
                "PC",
                "1041",
                DURATION,
                DESCRIPTION,
                POST_RUN_ACTION,
                ENVIRONMENT_CONFIGURATION_ID,
                null);
    }
    
    private boolean verifyTestsuitesFunctional(Testsuites testsuites, String runType) {
        
        boolean ret = true;
        Testcase testcase = testsuites.getTestsuite().get(0).getTestcase().get(0);
        ret =
                (testcase.getStatus().equals("pass"))
                        && (testcase.getName().equals("vapi1 (Test instance run ID: 7)"))
                        && (testcase.getClassname().equals(String.format(
                                "%s1 (RunId:1005).testset1",
                                runType))) && (testcase.getTime().equals("0.0"));
        
        return ret;
    }
    
    private boolean verifyTestsuitesPC(Testsuites testsuites, String runType) {
        
        boolean ret = true;
        Testcase testcase = testsuites.getTestsuite().get(0).getTestcase().get(0);
        ret =
                (testcase.getStatus().equals("error"))
                        && (testcase.getName().equals("Unnamed test (No test instance run ID)"))
                        && (testcase.getClassname().equals("PC Test ID: 5, Run ID: 42, Test Set ID: 1.(Unnamed test set)"))
                        && (testcase.getTime().equals("0.0"));
        
        return ret;
    }
}
