package com.hp.application.automation.bamboo.tasks;

import java.io.File;

/**
 * Created by dsinelnikov on 8/14/2015.
 */

public class ResultInfoItem
{
    private String _testName;
    public String getTestName()
    {
        return _testName;
    }

    private String _resultName;
    public String getResultName()
    {
        return _resultName;
    }

    private File _sourceDir;
    public File getSourceDir()
    {
        return _sourceDir;
    }

    private File _zipFile;
    public File getZipFile()
    {
        return _zipFile;
    }

    public ResultInfoItem(String testName, File sourceDir, File zipFile, String resultName)
    {
        _testName = testName;
        _resultName = resultName;
        _sourceDir = sourceDir;
        _zipFile = zipFile;
    }
}
