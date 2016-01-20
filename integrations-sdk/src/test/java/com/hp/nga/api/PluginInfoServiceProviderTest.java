package com.hp.octane.api;

import com.hp.nga.api.PluginInfoServiceProvider;
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
		com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest implA = new com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest();
		com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest implB = new com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest();
	}

	@Test(expected = RuntimeException.class)
	public void testB() {
		com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest implA = new com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest();
		implA = new com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest();
	}

	@Test(expected = RuntimeException.class)
	public void testC() {
		com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest implA = new com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplATest();
		com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplBTest implB = new com.hp.octane.api.PluginInfoServiceTest.PluginInfoServiceProviderImplBTest();
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
