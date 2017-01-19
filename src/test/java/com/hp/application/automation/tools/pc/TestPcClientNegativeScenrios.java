package com.hp.application.automation.tools.pc;

import hudson.FilePath;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.hp.application.automation.tools.run.PcBuilder;

public class TestPcClientNegativeScenrios {

    private static PcClient  pcClient;
    public final String      RESOURCES_DIR = getClass().getResource("").getPath();

    @Rule
    public ExpectedException exception     = ExpectedException.none();

    @BeforeClass
    public static void setUp() {
        System.out.println("Starting HP Performance Center client negative testing scenarios:");
        try {
            PcRestProxy resetProxy = new MockPcRestProxyBadResponses(PcTestBase.WEB_PROTOCOL,PcTestBase.PC_SERVER_NAME, PcTestBase.ALM_DOMAIN,
                PcTestBase.ALM_PROJECT,PcTestBase.LOGGER);
            pcClient = new PcClient(PcTestBase.pcModel, new PrintStream(new OutputStream() {
                
                @Override
                public void write(int b) throws IOException {}
                
            }), resetProxy);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("End of HP Performance Center client negative testing scenarios");
    }

    @Test
    public void testLoginWithWrongCredentials() {

        System.out.println("Testing Login to HP PC server with wrong credentials");
        Assert.assertFalse("Login to PC server with wrong creadentials should have failed", pcClient.login());
    }

    @Test(timeout = 5000)
    public void testHandleRunFailureWhileWaitingForRunCompletion() {

        System.out.println("Testing Wait For Run Completion with PC client while run fails");
        try {
            PcRunResponse response = pcClient.waitForRunCompletion(Integer.parseInt(PcTestBase.RUN_ID_WAIT), 200);
            Assert.assertEquals(response.getRunState(), RunState.RUN_FAILURE.value());
        } catch (InterruptedException e) {
            Assert.fail("pcClient did not return from waitForRunCompletion (test run has timed out)");
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testPublishRunReportWithEmptyResults() {

        System.out.println("Testing Publish PC Run Report to while run results are empty");
        try {

            FilePath reportHtml = pcClient.publishRunReport(Integer.parseInt(PcTestBase.RUN_ID),
                    String.format(PcBuilder.getRunReportStructure(), RESOURCES_DIR, PcBuilder.getArtifactsDirectoryName(),PcTestBase.RUN_ID));
            Assert.assertNull("pcClient.publishRunReport should have returned null due to empty run results",
                reportHtml);
        } catch (Exception e) {
            Assert.fail("pcClient.publishRunReport threw an exception (should have returned null due to empty run results): "
                        + e.toString());
        }
    }

    @Test
    public void testStopNonExistingRun() {
        
        System.out.println("Testing stopping a non-exising run with PC client");
        Assert.assertFalse("Stopping a non-existing run should have failed", pcClient.stopRun(Integer.parseInt(PcTestBase.RUN_ID)));
    }

}
