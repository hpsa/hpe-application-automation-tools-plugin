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

	public String getName();

	public void setName(String name);
	public boolean isBlocking();

	public void setBlocking(boolean blocking);

	public List<StructureItem> getJobs();

	public void setJobs(List<StructureItem> jobs);
}