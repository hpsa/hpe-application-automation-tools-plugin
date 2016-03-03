package com.hp.octane.plugins.jenkins;

import com.hp.nga.integrations.dto.causes.CIEventCauseType;
import com.hp.nga.integrations.dto.events.CIEventType;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.scm.SCMType;
import com.hp.nga.integrations.dto.snapshots.SnapshotResult;
import com.hp.nga.integrations.dto.snapshots.SnapshotStatus;
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
		assertEquals(CIEventType.values().length, 4);
		assertEquals(CIEventType.QUEUED.value(), "queued");
		assertEquals(CIEventType.STARTED.value(), "started");
		assertEquals(CIEventType.FINISHED.value(), "finished");
		assertEquals(CIEventType.fromValue("queued"), CIEventType.QUEUED);
	}

	@Test
	public void testParameterType() {
		assertEquals(ParameterType.values().length, 7);
		assertEquals(ParameterType.UNKNOWN.value(), "unknown");
		assertEquals(ParameterType.PASSWORD.value(), "password");
		assertEquals(ParameterType.BOOLEAN.value(), "boolean");
		assertEquals(ParameterType.STRING.value(), "string");
		assertEquals(ParameterType.NUMBER.value(), "number");
		assertEquals(ParameterType.FILE.value(), "file");
    assertEquals(ParameterType.AXIS.value(), "axis");
		assertEquals(ParameterType.fromValue("unavailable"), ParameterType.UNKNOWN);
	}

	@Test
	public void testSnapshotResult() {
		assertEquals(SnapshotResult.values().length, 5);
		assertEquals(SnapshotResult.UNAVAILABLE.value(), "unavailable");
		assertEquals(SnapshotResult.UNSTABLE.value(), "unstable");
		assertEquals(SnapshotResult.ABORTED.value(), "aborted");
		assertEquals(SnapshotResult.FAILURE.value(), "failure");
		assertEquals(SnapshotResult.SUCCESS.value(), "success");
		assertEquals(SnapshotResult.fromValue("unavailable"), SnapshotResult.UNAVAILABLE);
	}

	@Test
	public void testSnapshotStatus() {
		assertEquals(SnapshotStatus.values().length, 4);
		assertEquals(SnapshotStatus.UNAVAILABLE.value(), "unavailable");
		assertEquals(SnapshotStatus.QUEUED.value(), "queued");
		assertEquals(SnapshotStatus.RUNNING.value(), "running");
		assertEquals(SnapshotStatus.FINISHED.value(), "finished");
		assertEquals(SnapshotStatus.fromValue("unavailable"), SnapshotStatus.UNAVAILABLE);
	}

	@Test
	public void testSCMType() {
		assertEquals(SCMType.values().length, 2);
		assertEquals(SCMType.UNKNOWN.value(), "unknown");
		assertEquals(SCMType.GIT.value(), "git");
		assertEquals(SCMType.fromValue("unknown"), SCMType.UNKNOWN);
	}
}
