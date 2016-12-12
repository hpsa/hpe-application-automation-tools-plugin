package com.hp.octane.integrations.api;

import com.hp.octane.integrations.dto.api.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.api.connectivity.OctaneTaskAbridged;

/**
 * Created by gullery on 17/08/2015.
 * <p/>
 * Tasks Processor handles NGA tasks, both coming from abridged logic as well as plugin's REST call delegation.
 * Generally Tasks Processor assumed to be implemented as a singleton, and in any case it should be fully thread safe.
 */

public interface TasksProcessor {

	/**
	 * Initiates execution of NGA logic oriented task
	 *
	 * @param task
	 * @return
	 */
	OctaneResultAbridged execute(OctaneTaskAbridged task);
}
