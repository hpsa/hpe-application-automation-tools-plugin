package com.hp.nga.integrations.dto.stormRunner;

import com.hp.nga.integrations.dto.DTOBase;

import java.util.List;

/**
 * Created by lev on 31/05/2016.
 */
public interface TestSuite  extends DTOBase {
    List<Property> getProprties();
    TestSuite setProperties(List<Property> properties);
    List<TestCase> getTestCases();
    TestSuite setTestCases(List<TestCase> testCases);
}
