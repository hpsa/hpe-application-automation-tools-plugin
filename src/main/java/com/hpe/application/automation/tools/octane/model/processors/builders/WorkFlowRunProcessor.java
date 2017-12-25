/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
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
