package com.hp.nga.integrations.dto.pipelines;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

public interface StructurePhase {

	String getName();

	StructurePhase setName(String name);

	boolean isBlocking();

	StructurePhase setBlocking(boolean blocking);

	List<StructureItem> getJobs();

	StructurePhase setJobs(List<StructureItem> jobs);
}