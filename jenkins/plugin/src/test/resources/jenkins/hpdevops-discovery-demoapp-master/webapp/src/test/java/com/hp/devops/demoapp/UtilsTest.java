package com.hp.devops.demoapp;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/11/14
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class UtilsTest {

	@Test
	public void nodifyTestA() {
		String[] result = Utils.nodify(null);
		Assert.assertEquals(result.length, 0);
	}

	@Test
	public void nodifyTestB() {
		String[] result = Utils.nodify("");
		Assert.assertEquals(result.length, 0);
	}

	@Test
	public void nodifyTestC() {
		String[] result = Utils.nodify("/api");
		Assert.assertEquals(result.length, 0);
	}

	@Test
	public void nodifyTestD() {
		String[] result = Utils.nodify("/some");
		Assert.assertEquals(result.length, 1);
		Assert.assertEquals(result[0], "some");
	}

	@Test
	public void nodifyTestE() {
		String[] result = Utils.nodify("/api/some");
		Assert.assertEquals(result.length, 1);
		Assert.assertEquals(result[0], "some");
	}

	@Test
	public void nodifyTestF() {
		String[] result = Utils.nodify("/api/some/more");
		Assert.assertEquals(result.length, 2);
		Assert.assertEquals(result[0], "some");
		Assert.assertEquals(result[1], "more");
	}

	@Test
	public void nodifyTestG() {
		String[] result = Utils.nodify("//api///some/");
		Assert.assertEquals(result.length, 1);
		Assert.assertEquals(result[0], "some");
	}
}
