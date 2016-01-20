package com.hp.octane.api;

import com.hp.nga.api.PluginInfoService;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by gullery on 15/01/2016.
 */

public class PluginInfoServiceTest {

	@BeforeClass
	public static void testInitial() {
		try {
			PluginInfoService impl = PluginInfoService.getInstance();
			fail("the flow should not get to this point");
		} catch (RuntimeException re) {
			//  Do nothing here
		}
	}

	@Test(expected = RuntimeException.class)
	public void testA() {
		PluginInfoServiceImplATest implA = new PluginInfoServiceImplATest();
		PluginInfoServiceImplATest implB = new PluginInfoServiceImplATest();
	}

	@Test(expected = RuntimeException.class)
	public void testB() {
		PluginInfoServiceImplATest implA = new PluginInfoServiceImplATest();
		implA = new PluginInfoServiceImplATest();
	}

	@Test(expected = RuntimeException.class)
	public void testC() {
		PluginInfoServiceImplATest implA = new PluginInfoServiceImplATest();
		PluginInfoServiceImplBTest implB = new PluginInfoServiceImplBTest();
	}

	@Test
	public void testD() {
		PluginInfoService impl = PluginInfoService.getInstance();
		assertNotNull(impl);
	}

	private static class PluginInfoServiceImplATest extends PluginInfoService {

		public PluginInfoServiceImplATest() {
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

	private static class PluginInfoServiceImplBTest extends PluginInfoService {

		public PluginInfoServiceImplBTest() {
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
