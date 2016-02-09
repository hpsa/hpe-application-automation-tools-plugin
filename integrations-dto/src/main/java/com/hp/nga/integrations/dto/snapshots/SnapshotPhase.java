package com.hp.nga.integrations.dto.snapshots;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

public interface SnapshotPhase {

	String getName();

	SnapshotPhase setName(String name);

	boolean isBlocking();

	SnapshotPhase setBlocking(boolean blocking);

	List<SnapshotItem> getBuilds();

	SnapshotPhase setBuilds(List<SnapshotItem> builds);
}