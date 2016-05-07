package com.hp.devops.demoapp;/**
 * Created with IntelliJ IDEA.
 * User: belozovs
 * Date: 11/25/14
 * Time: 4:32 PM
 * To change this template use File | Settings | File Templates.
 */

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.*;

import java.util.HashMap;
import java.util.List;

import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestServletTest {

	private static RequestSpecification spec;
	private static ConfigurationService configurationService = ConfigurationService.getInstance();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RestAssured.baseURI = configurationService.getBaseUri();
		RestAssured.port = configurationService.getPort();
		RestAssured.basePath = configurationService.getBasePath();
		if (!configurationService.getProxyHost().isEmpty()) {
			RestAssured.proxy(configurationService.getProxyHost(), configurationService.getProxyPort());
		}

		spec = RestAssured.given();
		System.out.println("Base URI: " + configurationService.getBaseUri());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetBands() throws Exception {
		String result = spec.log().all().expect().statusCode(isOneOf(200)).get("/bands").asString();
		List bandsList = from(result).get("");
		assertEquals("We should have 5 bands", 5, bandsList.size());
	}

	@Test
	public void testReloadDb() throws Exception {
		spec.log().all().expect().statusCode(200).contentType(ContentType.TEXT).body(equalTo("done")).get("/reloadDB");
	}

	@Test
	public void testVoteForBand() throws Exception {
		String response1 = spec.log().all().expect().statusCode(200).when().put("/band/1/vote").asString();
		List votesList1 = from(response1).get("");
		int votes1 = ((HashMap<String, Integer>) votesList1.get(0)).get("votes");
		System.out.println("Votes1: " + votesList1.get(0));
		String response2 = spec.log().all().expect().statusCode(200).when().put("/band/1/vote").asString();
		List votesList2 = from(response2).get("");
		int votes2 = ((HashMap<String, Integer>) votesList2.get(0)).get("votes");
		System.out.println("Votes2: " + votesList2.get(0));
		int diff = votes2 - votes1;
		assertTrue("Votes should increase by 1", diff == 1);
	}

	@Test
	public void testMultipleVotingBlocked() throws Exception {
		String cookieValue;
		cookieValue = spec.log().all().expect().statusCode(isOneOf(200)).get("/bands").getCookie("hpDevopsDemoApp");
		assertTrue("cookie value should be equal 'null'", cookieValue == null);
		cookieValue = spec.log().all().expect().statusCode(200).when().put("/band/1/vote").getCookie("hpDevopsDemoApp");
		assertTrue("cookie value should be equal 'true'", cookieValue.compareTo("true") == 0);
	}
}
