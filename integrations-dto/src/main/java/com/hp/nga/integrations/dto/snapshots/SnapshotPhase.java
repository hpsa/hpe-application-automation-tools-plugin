package com.hp.nga.integrations.dto.snapshots;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.nga.integrations.dto.DTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = SnapshotPhaseImpl.class, name = "SnapshotPhaseImpl"))
public interface SnapshotPhase extends DTO {

	String getName();

	SnapshotPhase setName(String name);

	boolean isBlocking();

	SnapshotPhase setBlocking(boolean blocking);

	List<SnapshotNode> getBuilds();

	SnapshotPhase setBuilds(List<SnapshotNode> builds);
}