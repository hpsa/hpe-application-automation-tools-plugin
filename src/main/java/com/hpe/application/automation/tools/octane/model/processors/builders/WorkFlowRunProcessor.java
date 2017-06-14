/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.model.processors.builders;

import com.google.common.util.concurrent.ListenableFuture;
import com.hpe.application.automation.tools.octane.workflow.WorkflowGraphListener;
import hudson.model.Run;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Created by gadiel on 21/07/2016.
 */

public class WorkFlowRunProcessor {
	private static final Logger logger = LogManager.getLogger(WorkFlowRunProcessor.class);
	WorkflowRun workFlowRun;

	public WorkFlowRunProcessor(Run r) {
		this.workFlowRun = (WorkflowRun) r;
	}

	public void registerEvents(ExecutorService executor) {
		ListenableFuture<FlowExecution> promise = workFlowRun.getExecutionPromise();
		promise.addListener(new Runnable() {
			@Override
			public void run() {
				try {
					FlowExecution ex = workFlowRun.getExecutionPromise().get();
					ex.addListener(new WorkflowGraphListener());
				} catch (InterruptedException ie) {
					logger.error("failed to obtain execution promise of " + workFlowRun, ie);
				} catch (ExecutionException ee) {
					logger.error("failed to obtain execution promise of " + workFlowRun, ee);
				}
			}
		}, executor);
	}
}
