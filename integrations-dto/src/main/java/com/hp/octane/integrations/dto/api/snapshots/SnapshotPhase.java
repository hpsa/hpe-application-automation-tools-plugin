package com.hp.octane.integrations.dto.api.snapshots;

import com.hp.octane.integrations.dto.DTOBase;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

public interface SnapshotPhase extends DTOBase {

	String getName();

	SnapshotPhase setName(String name);

	boolean isBlocking();

	SnapshotPhase setBlocking(boolean blocking);

	List<SnapshotNode> getBuilds();

	SnapshotPhase setBuilds(List<SnapshotNode> builds);
}