package com.hp.octane.plugins.jenkins.model.processors.builders;

import com.google.common.util.concurrent.ListenableFuture;
import com.hp.octane.plugins.jenkins.workflow.WorkflowGraphListener;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Created by gadiel on 21/07/2016.
 */
public class WorkFlowRunProcessor {
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
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}, executor);
	}
}
