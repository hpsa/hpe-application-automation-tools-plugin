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

package com.hp.application.automation.tools.octane.actions;

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
