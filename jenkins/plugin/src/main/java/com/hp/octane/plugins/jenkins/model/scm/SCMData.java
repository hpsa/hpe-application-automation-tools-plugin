package com.hp.octane.plugins.jenkins.model.scm;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/10/14
 * Time: 22:33
 * To change this template use File | Settings | File Templates.
 */
public class SCMData {
	private ArrayList<SCMRepository> repositories;

	public SCMData(ArrayList<SCMRepository> repositories) {
		this.repositories = repositories;
	}

	public ArrayList<SCMRepository> getRepositories() {
		return repositories;
	}
}
