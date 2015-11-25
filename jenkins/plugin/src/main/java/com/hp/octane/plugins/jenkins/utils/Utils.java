package com.hp.octane.plugins.jenkins.utils;

import java.util.Calendar;

/**
 * Created by gullery on 25/11/2015.
 * <p/>
 * Utils class to hold static only methods for general usage
 */

public final class Utils {
	private Utils() {
	}

	static public long timestampInUTC(long input) {
		return input - Calendar.getInstance().getTimeZone().getRawOffset();
	}
}
