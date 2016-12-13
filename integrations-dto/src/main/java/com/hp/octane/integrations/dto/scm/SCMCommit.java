package com.hp.octane.integrations.dto.scm;

import com.hp.octane.integrations.dto.DTOBase;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:51
 * To change this template use File | Settings | File Templates.
 */

public interface SCMCommit extends DTOBase {

	Long getTime();

	SCMCommit setTime(Long time);

	String getUser();

	SCMCommit setUser(String user);

	String getUserEmail();

	SCMCommit setUserEmail(String userEmail);

	String getRevId();

	SCMCommit setRevId(String revId);

	String getParentRevId();

	SCMCommit setParentRevId(String parentRevId);

	String getComment();

	SCMCommit setComment(String comment);

	List<SCMChange> getChanges();

	SCMCommit setChanges(List<SCMChange> changes);
}
