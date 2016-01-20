package com.hp.nga.api;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by gullery on 15/01/2016.
 */

public class PluginInfoServiceProviderTest {

	@BeforeClass
	public static void testInitial() {
		try {
			PluginInfoServiceProvider impl = PluginInfoServiceProvider.getInstance();
			fail("the flow should not get to this point");
		} catch (RuntimeException re) {
			//  Do nothing here
		}
	}

	@Test(expected = RuntimeException.class)
	public void testA() {
		PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest implA = new PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest();
		PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest implB = new PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest();
	}

	@Test(expected = RuntimeException.class)
	public void testB() {
		PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest implA = new PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest();
		implA = new PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest();
	}

	@Test(expected = RuntimeException.class)
	public void testC() {
		PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest implA = new PluginInfoServiceProviderTest.PluginInfoServiceProviderImplATest();
		PluginInfoServiceProviderTest.PluginInfoServiceProviderImplBTest implB = new PluginInfoServiceProviderTest.PluginInfoServiceProviderImplBTest();
	}

	@Test
	public void testD() {
		PluginInfoServiceProvider impl = PluginInfoServiceProvider.getInstance();
		assertNotNull(impl);
	}

	private static class PluginInfoServiceProviderImplATest extends PluginInfoServiceProvider {

		public PluginInfoServiceProviderImplATest() {
			super();
		}

		@Override
		public String getOwnUrl() {
			return null;
		}

		@Override
		public String getInstanceId() {
			return null;
		}
	}

	private static class PluginInfoServiceProviderImplBTest extends PluginInfoServiceProvider {

		public PluginInfoServiceProviderImplBTest() {
			super();
		}

		@Override
		public String getOwnUrl() {
			return null;
		}

		@Override
		public String getInstanceId() {
			return null;
		}
	}
}
