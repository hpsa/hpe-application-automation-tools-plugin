package com.hp.nga.integrations.api;

import com.hp.nga.integrations.SDKServicePublic;
import com.hp.octane.integrations.dto.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;

/**
 * Created by gullery on 17/08/2015.
 * <p/>
 * Tasks Processor handles NGA tasks, both coming from abridged logic as well as plugin's REST call delegation.
 * Generally Tasks Processor assumed to be implemented as a singleton, and in any case it should be fully thread safe.
 */

public interface TasksProcessor extends SDKServicePublic {

	/**
	 * Initiates execution of NGA logic oriented task
	 *
	 * @param task
	 * @return
	 */
	OctaneResultAbridged execute(OctaneTaskAbridged task);
}
