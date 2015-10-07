import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseType;
import com.hp.octane.plugins.jenkins.model.events.CIEventType;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import com.hp.octane.plugins.jenkins.model.snapshots.SnapshotResult;
import com.hp.octane.plugins.jenkins.model.snapshots.SnapshotStatus;
import com.hp.octane.plugins.jenkins.model.scm.SCMType;
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
		assertEquals(CIEventCauseType.SCM.toString(), "scm");
		assertEquals(CIEventCauseType.USER.toString(), "user");
		assertEquals(CIEventCauseType.TIMER.toString(), "timer");
		assertEquals(CIEventCauseType.UPSTREAM.toString(), "upstream");
		assertEquals(CIEventCauseType.UNDEFINED.toString(), "undefined");
		assertEquals(CIEventCauseType.getByValue("scm"), CIEventCauseType.SCM);
	}

	@Test
	public void testCIEventType() {
		assertEquals(CIEventType.values().length, 3);
		assertEquals(CIEventType.QUEUED.toString(), "queued");
		assertEquals(CIEventType.STARTED.toString(), "started");
		assertEquals(CIEventType.FINISHED.toString(), "finished");
		assertEquals(CIEventType.getByValue("queued"), CIEventType.QUEUED);
	}

	@Test
	public void testParameterType() {
		assertEquals(ParameterType.values().length, 7);
		assertEquals(ParameterType.UNKNOWN.toString(), "unknown");
		assertEquals(ParameterType.PASSWORD.toString(), "password");
		assertEquals(ParameterType.BOOLEAN.toString(), "boolean");
		assertEquals(ParameterType.STRING.toString(), "string");
		assertEquals(ParameterType.NUMBER.toString(), "number");
		assertEquals(ParameterType.FILE.toString(), "file");
    assertEquals(ParameterType.AXIS.toString(), "axis");
		assertEquals(ParameterType.getByValue("unavailable"), ParameterType.UNKNOWN);
	}

	@Test
	public void testSnapshotResult() {
		assertEquals(SnapshotResult.values().length, 5);
		assertEquals(SnapshotResult.UNAVAILABLE.toString(), "unavailable");
		assertEquals(SnapshotResult.UNSTABLE.toString(), "unstable");
		assertEquals(SnapshotResult.ABORTED.toString(), "aborted");
		assertEquals(SnapshotResult.FAILURE.toString(), "failure");
		assertEquals(SnapshotResult.SUCCESS.toString(), "success");
		assertEquals(SnapshotResult.getByValue("unavailable"), SnapshotResult.UNAVAILABLE);
	}

	@Test
	public void testSnapshotStatus() {
		assertEquals(SnapshotStatus.values().length, 4);
		assertEquals(SnapshotStatus.UNAVAILABLE.toString(), "unavailable");
		assertEquals(SnapshotStatus.QUEUED.toString(), "queued");
		assertEquals(SnapshotStatus.RUNNING.toString(), "running");
		assertEquals(SnapshotStatus.FINISHED.toString(), "finished");
		assertEquals(SnapshotStatus.getByValue("unavailable"), SnapshotStatus.UNAVAILABLE);
	}

	@Test
	public void testSCMType() {
		assertEquals(SCMType.values().length, 2);
		assertEquals(SCMType.UNSUPPORTED.toString(), "unsupported");
		assertEquals(SCMType.GIT.toString(), "git");
		assertEquals(SCMType.getByValue("unsupported"), SCMType.UNSUPPORTED);
	}
}
