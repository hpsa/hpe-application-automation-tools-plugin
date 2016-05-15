package com.hp.devops.demoapp;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.security.InvalidParameterException;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/11/14
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class DataManagerTest {

	@Test
	public void dataManagerTestA() {
		try {
			DataManager.init(null);
			Assert.fail("the flow MUST have been fallen before");
		} catch (Exception e) {
			Assert.assertEquals(e.getClass(), InvalidParameterException.class);
			Assert.assertEquals(e.getMessage(), "servletContext must not be null");
		}
	}

	@Test
	public void dataManagerTestB() {
		DataManager.loadData();
		Assert.assertEquals(DataManager.isInitialized(), false);
	}

	@Test
	public void dataManagerTestC() {
		try {
			DataManager.getAll();
			Assert.fail("the flow MUST have been fallen before");
		} catch (Exception e) {
			Assert.assertEquals(e.getClass(), Exception.class);
			Assert.assertEquals(e.getMessage(), "service not initialized");
		}
	}

	@Test
	public void dataManagerTestD() {
		try {
			DataManager.getBand(0);
			Assert.fail("the flow MUST have been fallen before");
		} catch (Exception e) {
			Assert.assertEquals(e.getClass(), Exception.class);
			Assert.assertEquals(e.getMessage(), "service not initialized");
		}
	}

	@Test
	public void dataManagerTestE() {
		try {
			DataManager.upVoteBand(0);
			Assert.fail("the flow MUST have been fallen before");
		} catch (Exception e) {
			Assert.assertEquals(e.getClass(), Exception.class);
			Assert.assertEquals(e.getMessage(), "service not initialized");
		}
	}
}
