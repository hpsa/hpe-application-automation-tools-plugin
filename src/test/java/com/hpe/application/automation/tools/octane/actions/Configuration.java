package com.hpe.application.automation.tools.octane.actions;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 12:05
 * To change this template use File | Settings | File Templates.
 */

public class Configuration {
	public static enum OS {
		WINDOWS("windows"),
		LINUX("linux");

		private String value;
		private static OS current;

		private OS(String v) {
			value = v;
		}

		public static OS getCurrent() {
			String tmpVal;
			if (current == null) {
				tmpVal = System.getProperty("os.name").toLowerCase();
				if (tmpVal.indexOf("windows") == 0) current = WINDOWS;
				else if (tmpVal.indexOf("linux") == 0) current = LINUX;
				else current = LINUX;
			}
			return current;
		}
	}
}
