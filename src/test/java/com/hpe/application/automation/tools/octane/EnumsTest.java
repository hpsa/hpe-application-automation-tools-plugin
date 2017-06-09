/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane;

import com.hp.octane.integrations.dto.causes.CIEventCauseType;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.integrations.dto.scm.SCMType;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.integrations.dto.snapshots.CIBuildStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"squid:S2699","squid:S3658","squid:S2259","squid:S1872","squid:S2925","squid:S109","squid:S1607","squid:S2701","squid:S2698"})
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
