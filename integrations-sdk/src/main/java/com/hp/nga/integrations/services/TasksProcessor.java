package com.hp.nga.integrations.services;

import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;

/**
 * Created by gullery on 17/08/2015.
 * <p>
 * Tasks Processor handles NGA tasks, both coming from abridged logic as well as plugin's REST call delegation.
 * Generally Tasks Processor assumed to be implemented as a singleton, and in any case it should be fully thread safe.
 */

public interface TasksProcessor {

	NGAResultAbridged execute(NGATaskAbridged task);
}
