package com.hp.devops.demoapp;

import org.eclipse.jetty.util.resource.FileResource;
import org.json.JSONArray;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/11/14
 * Time: 09:58
 * To change this template use File | Settings | File Templates.
 */
public class DataManager {

	static private boolean initialized = false;
	static private Object lock = new Object();
	static private String resourcePath = "/WEB-INF/fdb/bands.json";
	static private ServletContext servletContext;
	static private ArrayList<Band> bands = new ArrayList<Band>();

	static void init(ServletContext servletContext) {
		if (servletContext != null) {
			DataManager.servletContext = servletContext;
			DataManager.loadData();
		} else {
			throw new InvalidParameterException("servletContext must not be null");
		}
	}

	static boolean isInitialized() {
		return initialized;
	}

	static void loadData() {
		try {
			if (servletContext == null) throw new Exception("service not initialized");
			FileResource resource = new FileResource(servletContext.getResource(resourcePath));
			InputStream inputStream = resource.getInputStream();
			String content = "";
			byte[] buffer = new byte[1024];
			int available;
			while ((available = inputStream.available()) > 0) {
				inputStream.read(buffer, 0, available);
				content += new String(buffer, 0, available);
			}
			JSONArray data = new JSONArray(content);
			synchronized (bands) {
				bands = new ArrayList<Band>();
				for (int i = 0; i < data.length(); i++) {
					bands.add(new Band(data.getJSONObject(i)));
				}
			}
			DataManager.initialized = true;
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (URISyntaxException uriException) {
			uriException.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static private void saveData() {
		JSONArray json = new JSONArray();
		synchronized (bands) {
			for (Band band : bands) {
				json.put(band.toJSON());
			}
		}
		try {
			synchronized (lock) {
				FileResource resource = new FileResource(servletContext.getResource(resourcePath));
				File file = resource.getFile();
				file.renameTo(new File(file.getAbsolutePath() + ".bak"));

				//  do i need it?
				file.createNewFile();
				PrintWriter output = new PrintWriter(file.getAbsolutePath());
				output.print(json.toString(4));
				output.close();
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (URISyntaxException uriException) {
			uriException.printStackTrace();
		}
	}

	static Band[] getAll() throws Exception {
		if (!initialized) throw new Exception("service not initialized");
		return bands.toArray(new Band[bands.size()]);
	}

	static Band getBand(int id) throws Exception {
		if (!initialized) throw new Exception("service not initialized");
		for (Band band : bands) {
			if (band.id == id) return band;
		}
		return null;
	}

	static void upVoteBand(int id) throws Exception {
		if (!initialized) throw new Exception("service not initialized");
		Band band = DataManager.getBand(id);
		if (band != null) {
			synchronized (band) {
				band.votes++;
			}
			saveData();
		} else {
			throw new Exception("the band not exist");
		}
	}
}
