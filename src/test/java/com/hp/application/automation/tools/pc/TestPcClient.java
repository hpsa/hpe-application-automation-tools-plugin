package com.hp.application.automation.tools.pc;

import hudson.FilePath;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.application.automation.tools.run.PcBuilder;

public class TestPcClient {
         
    private static PcClient pcClient;
    public final String RESOURCES_DIR = getClass().getResource("").getPath();

    @BeforeClass
    public static void setUp() {
        try {
            PcRestProxy resetProxy = new MockPcRestProxy(PcTestBase.PC_SERVER_NAME, PcTestBase.ALM_DOMAIN,
                PcTestBase.ALM_PROJECT);
            pcClient = new PcClient(PcTestBase.pcModel, System.out, resetProxy);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    @Test
    public void testLogin(){
        System.out.println("Testing Login to HP PC server");
        Assert.assertTrue("Failed to login with pcClient", pcClient.login());  
    }

    @Test
    public void testStartRun(){
        System.out.println("Testing Start Run with PC client");
        try {
            Assert.assertTrue("Failed to start run with pcClient", pcClient.startRun() > 0);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }       
    }

    @Test (timeout=5000)
    public void testWaitForRunCompletion(){
        
        System.out.println("Testing Wait for Run Completion with PC client");
        try {
            PcRunResponse response = pcClient.waitForRunCompletion(Integer.parseInt(PcTestBase.RUN_ID_WAIT), 200);
            Assert.assertEquals(response.getRunState(), RunState.FINISHED.value());
        } catch (InterruptedException e) {            
            Assert.fail("pcClient did not return from waitForRunCompletion (test run has timed out)");
        }catch (Exception e) {
            Assert.fail(e.toString());
        }       
    }
    
    @Test
    public void testPublishRunReport(){
        
        System.out.println("Testing Publish PC Run Report to Jenkins server with PC client");
        try {
            
            FilePath reportHtml = pcClient.publishRunReport(Integer.parseInt(PcTestBase.RUN_ID),
                String.format(PcBuilder.getRunReportStructure(), RESOURCES_DIR, PcBuilder.getArtifactsDirectoryName(), PcTestBase.RUN_ID));
            Assert.assertTrue("Failed to publish PC run report", reportHtml.exists());
            try {
                // Test cleanup
                reportHtml.getParent().getParent().getParent().deleteRecursive();
            } catch (Exception e) {
            }
        } catch (Exception e) {
            Assert.fail(e.toString());
        }       
    }    
    
    @Test
    public void testLogout() {        
        System.out.println("Testing Logout from HP PC server");
        Assert.assertTrue("Failed to logout with pcClient", pcClient.logout());
    }

    @Test
    public void testStopRun() {        
        System.out.println("Testing Stop Run with PC client");
        Assert.assertTrue("Failed to stop run with pcClient", pcClient.stopRun(Integer.parseInt(PcTestBase.RUN_ID)));
    }
    
    
}
  
