package com.hp.octane.plugins.jenkins.utils;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

/**
 * Created by gullery on 25/11/2015.
 */

public class UtilsTest {

	@Test
	public void testA() {
		int offset = Calendar.getInstance().getTimeZone().getRawOffset();
		long currentNonUTC = System.currentTimeMillis();

		assertEquals(currentNonUTC - offset, Utils.timestampInUTC(currentNonUTC));
	}
}
