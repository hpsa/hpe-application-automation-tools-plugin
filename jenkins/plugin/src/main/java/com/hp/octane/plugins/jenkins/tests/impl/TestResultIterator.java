package com.hp.octane.plugins.jenkins.tests.impl;

import com.hp.octane.plugins.jenkins.tests.TestResult;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFields;
import hudson.FilePath;

import java.io.IOException;

public class TestResultIterator extends ObjectStreamIterator<TestResult> {

    private ResultFields resultFields;

    public TestResultIterator(FilePath filePath, boolean deleteOnClose, ResultFields resultFields) throws IOException, InterruptedException {
        super(filePath, deleteOnClose);
        this.resultFields = resultFields;
    }

    public ResultFields getResultFields() {
        return resultFields;
    }
}
