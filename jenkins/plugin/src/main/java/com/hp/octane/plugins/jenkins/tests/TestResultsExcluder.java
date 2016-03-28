package com.hp.octane.plugins.jenkins.tests;

/**
 * Created by franksha on 21/03/2016.
 */
public interface TestResultsExcluder {
  boolean shouldExclude(TestResult testResult);
}
