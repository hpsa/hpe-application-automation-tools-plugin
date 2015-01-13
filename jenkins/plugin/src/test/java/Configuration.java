/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 12:05
 * To change this template use File | Settings | File Templates.
 */

public class Configuration {
	private static final int WINDOWS = 0;
	private static final int LINUX = 1;

	private static int OS;

	static {
		String system = System.getProperty("platform");
		if (system == null || system.compareTo("windows") == 0) OS = WINDOWS;
		else if (system.compareTo("linux") == 0) OS = LINUX;
		else OS = WINDOWS;
	}

	static String getSleepScript(int seconds) {
		if (OS == WINDOWS) {
			return "ping -n " + seconds + " 127.0.0.1 >nul";
		} else if (OS == LINUX) {
			return "sleep " + seconds;
		} else {
			return "";
		}
	}
}
