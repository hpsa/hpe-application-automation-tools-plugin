package com.hp.octane.plugins.jenkins.apis;

import com.hp.octane.plugins.jenkins.model.pipeline.Snapshot;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */
public interface IPipeline extends IJSONable {

	Snapshot getSnapshot(int number);

	Snapshot getLastSnapshot();

	void run();

}
