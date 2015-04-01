package com.hp.octane.plugins.jenkins.model.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/10/14
 * Time: 22:33
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class SCMData {
	private ArrayList<SCMRepository> repositories;

	public SCMData(ArrayList<SCMRepository> repositories) {
		this.repositories = repositories;
	}

	@Exported(inline = true)
	public SCMRepository[] getRepositories() {
		return repositories.toArray(new SCMRepository[repositories.size()]);
	}
}
