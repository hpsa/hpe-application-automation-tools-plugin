package com.hp.nga.api;

import com.hp.nga.dto.builds.SnapshotDTO;

/**
 * Created by gullery on 07/01/2016.
 * <p/>
 * API definition of Builds Service
 */

public abstract class BuildsServiceProvider {
	public abstract SnapshotDTO getLatestSnapshot(String projectCiId, String buildCiId, boolean deepTree);
}
