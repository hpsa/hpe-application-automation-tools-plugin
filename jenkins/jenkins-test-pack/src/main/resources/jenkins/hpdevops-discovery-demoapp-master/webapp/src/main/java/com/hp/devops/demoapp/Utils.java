package com.hp.devops.demoapp;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/11/14
 * Time: 09:58
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

	static String[] nodify(String input) {
		ArrayList<String> nodes = new ArrayList<String>();
		if (input != null) {
			for (String node : input.split("/")) {
				if (node.compareTo("") != 0 && node.compareTo("api") != 0) {
					nodes.add(node);
				}
			}
		}
		return nodes.toArray(new String[nodes.size()]);
	}
}
