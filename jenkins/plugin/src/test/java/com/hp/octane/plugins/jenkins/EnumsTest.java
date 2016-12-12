package com.hp.octane.plugins.jenkins;

import com.hp.octane.integrations.dto.api.causes.CIEventCauseType;
import com.hp.octane.integrations.dto.api.events.CIEventType;
import com.hp.octane.integrations.dto.api.parameters.CIParameterType;
import com.hp.octane.integrations.dto.api.scm.SCMType;
import com.hp.octane.integrations.dto.api.snapshots.CIBuildResult;
import com.hp.octane.integrations.dto.api.snapshots.CIBuildStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */

public class EnumsTest {

	@Test
	public void testCIEventCauseType() {
		assertEquals(CIEventCauseType.values().length, 5);
		assertEquals(CIEventCauseType.SCM.value(), "scm");
		assertEquals(CIEventCauseType.USER.value(), "user");
		assertEquals(CIEventCauseType.TIMER.value(), "timer");
		assertEquals(CIEventCauseType.UPSTREAM.value(), "upstream");
		assertEquals(CIEventCauseType.UNDEFINED.value(), "undefined");
		assertEquals(CIEventCauseType.fromValue("scm"), CIEventCauseType.SCM);
	}

	@Test
	public void testCIEventType() {
		assertEquals(CIEventType.values().length, 5);
		assertEquals(CIEventType.QUEUED.value(), "queued");
		assertEquals(CIEventType.SCM.value(), "scm");
		assertEquals(CIEventType.STARTED.value(), "started");
		assertEquals(CIEventType.FINISHED.value(), "finished");
		assertEquals(CIEventType.fromValue("queued"), CIEventType.QUEUED);
	}

	@Test
	public void testParameterType() {
		assertEquals(CIParameterType.values().length, 7);
		assertEquals(CIParameterType.UNKNOWN.value(), "unknown");
		assertEquals(CIParameterType.PASSWORD.value(), "password");
		assertEquals(CIParameterType.BOOLEAN.value(), "boolean");
		assertEquals(CIParameterType.STRING.value(), "string");
		assertEquals(CIParameterType.NUMBER.value(), "number");
		assertEquals(CIParameterType.FILE.value(), "file");
    assertEquals(CIParameterType.AXIS.value(), "axis");
		assertEquals(CIParameterType.fromValue("unavailable"), CIParameterType.UNKNOWN);
	}

	@Test
	public void testSnapshotResult() {
		assertEquals(CIBuildResult.values().length, 5);
		assertEquals(CIBuildResult.UNAVAILABLE.value(), "unavailable");
		assertEquals(CIBuildResult.UNSTABLE.value(), "unstable");
		assertEquals(CIBuildResult.ABORTED.value(), "aborted");
		assertEquals(CIBuildResult.FAILURE.value(), "failure");
		assertEquals(CIBuildResult.SUCCESS.value(), "success");
		assertEquals(CIBuildResult.fromValue("unavailable"), CIBuildResult.UNAVAILABLE);
	}

	@Test
	public void testSnapshotStatus() {
		assertEquals(CIBuildStatus.values().length, 4);
		assertEquals(CIBuildStatus.UNAVAILABLE.value(), "unavailable");
		assertEquals(CIBuildStatus.QUEUED.value(), "queued");
		assertEquals(CIBuildStatus.RUNNING.value(), "running");
		assertEquals(CIBuildStatus.FINISHED.value(), "finished");
		assertEquals(CIBuildStatus.fromValue("unavailable"), CIBuildStatus.UNAVAILABLE);
	}

	@Test
	public void testSCMType() {
		assertEquals(SCMType.values().length, 3);
		assertEquals(SCMType.UNKNOWN.value(), "unknown");
		assertEquals(SCMType.GIT.value(), "git");
		assertEquals(SCMType.SVN.value(), "svn");
		assertEquals(SCMType.fromValue("unknown"), SCMType.UNKNOWN);
	}
}
