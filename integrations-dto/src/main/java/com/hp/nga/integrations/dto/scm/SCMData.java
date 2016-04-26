package com.hp.nga.integrations.dto.scm;

import com.hp.nga.integrations.dto.DTOBase;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/10/14
 * Time: 22:33
 * To change this template use File | Settings | File Templates.
 */

public interface SCMData extends DTOBase {

	SCMRepository getRepository();

	SCMData setRepository(SCMRepository repository);

	String getBuiltRevId();

	SCMData setBuiltRevId(String builtRevId);

	List<SCMCommit> getCommits();

	SCMData setCommits(List<SCMCommit> commits);
}
