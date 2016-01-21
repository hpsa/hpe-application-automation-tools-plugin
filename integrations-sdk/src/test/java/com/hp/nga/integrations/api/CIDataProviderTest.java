package com.hp.nga.integrations.api;

import com.hp.nga.integrations.dto.builds.SnapshotDTO;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;
import com.hp.nga.integrations.dto.projects.ProjectsList;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by gullery on 15/01/2016.
 */

public class CIDataProviderTest {

	@BeforeClass
	public static void testInitial() {
		try {
			CIDataProvider impl = CIDataProvider.getInstance();
			fail("the flow should not get to this point");
		} catch (RuntimeException re) {
			//  Do nothing here
		}
	}

	@Test(expected = RuntimeException.class)
	public void testA() {
		CIDataProviderTest.PluginInfoServiceProviderImplATest implA = new CIDataProviderTest.PluginInfoServiceProviderImplATest();
		CIDataProviderTest.PluginInfoServiceProviderImplATest implB = new CIDataProviderTest.PluginInfoServiceProviderImplATest();
	}

	@Test(expected = RuntimeException.class)
	public void testB() {
		CIDataProviderTest.PluginInfoServiceProviderImplATest implA = new CIDataProviderTest.PluginInfoServiceProviderImplATest();
		implA = new CIDataProviderTest.PluginInfoServiceProviderImplATest();
	}

	@Test(expected = RuntimeException.class)
	public void testC() {
		CIDataProviderTest.PluginInfoServiceProviderImplATest implA = new CIDataProviderTest.PluginInfoServiceProviderImplATest();
		CIDataProviderTest.PluginInfoServiceProviderImplBTest implB = new CIDataProviderTest.PluginInfoServiceProviderImplBTest();
	}

	@Test
	public void testD() {
		CIDataProvider impl = CIDataProvider.getInstance();
		assertNotNull(impl);
	}

	private static class PluginInfoServiceProviderImplATest extends CIDataProvider {

		public PluginInfoServiceProviderImplATest() {
			super();
		}

		@Override
		public ServerInfo getServerInfo() {
			return null;
		}

		@Override
		public PluginInfo getPluginInfo() {
			return null;
		}

		@Override
		public ProjectsList getProjectsList(boolean includeParameters) {
			return null;
		}

		@Override
		public SnapshotDTO getLatestSnapshot(String ciProjectId, String ciBuildId, boolean subTree) {
			return null;
		}
	}

	private static class PluginInfoServiceProviderImplBTest extends CIDataProvider {

		public PluginInfoServiceProviderImplBTest() {
			super();
		}

		@Override
		public ServerInfo getServerInfo() {
			return null;
		}

		@Override
		public PluginInfo getPluginInfo() {
			return null;
		}

		@Override
		public ProjectsList getProjectsList(boolean includeParameters) {
			return null;
		}

		@Override
		public SnapshotDTO getLatestSnapshot(String ciProjectId, String ciBuildId, boolean subTree) {
			return null;
		}
	}
}
