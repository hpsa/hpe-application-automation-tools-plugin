package com.hp.octane.plugins.jenkins.rest;

import org.kohsuke.stapler.StaplerRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by gullery on 04/01/2016.
 */

public class RESTUtils {
	static String readBody(StaplerRequest req) throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int readLen;
		while ((readLen = req.getInputStream().read(buffer)) > 0) {
			byteArray.write(buffer, 0, readLen);
		}
		return new String(byteArray.toByteArray());
	}
}
