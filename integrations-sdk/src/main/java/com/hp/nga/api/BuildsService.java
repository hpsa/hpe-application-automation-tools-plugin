package com.hp.nga.api;

/**
 * Created by gullery on 07/01/2016.
 * <p/>
 * API definition of Builds Service
 */

public abstract class BuildsService {
	public abstract SnapshotDTO getLatestSnapshot(String projectCiId, String buildCiId, boolean deepTree);
}
