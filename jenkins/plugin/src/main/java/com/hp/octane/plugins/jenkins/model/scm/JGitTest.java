package com.hp.octane.plugins.jenkins.model.scm;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;

/**
 * Created by gullery on 04/08/2015.
 */

public class JGitTest {

	public static void readRepo(String repoFolder) throws IOException {
		Repository repository = Git.open(new File(repoFolder)).getRepository();
		RevWalk revWalk = new RevWalk(repository);
		while (revWalk.iterator().hasNext()) {
			RevCommit next = revWalk.iterator().next();
			System.out.println(next.getId() + " - " + next.getFullMessage());
		}
	}
}
