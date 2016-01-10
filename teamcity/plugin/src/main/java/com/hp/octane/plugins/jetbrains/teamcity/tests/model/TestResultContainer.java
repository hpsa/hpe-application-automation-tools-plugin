package com.hp.octane.plugins.jetbrains.teamcity.tests.model;

import java.util.Iterator;

public class TestResultContainer {

    private Iterator<TestResult> iterator;
    private ResultFields resultFields;

    public TestResultContainer(Iterator<TestResult> iterator, ResultFields resultFields) {
        this.iterator = iterator;
        this.resultFields = resultFields;
    }

    public Iterator<TestResult> getIterator() {
        return iterator;
    }

    public ResultFields getResultFields() {
        return resultFields;
    }
}
