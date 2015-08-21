package com.hp.application.automation.bamboo.tasks;

import java.io.File;

/**
 * Created by dsinelnikov on 8/14/2015.
 */

public class ResultInfoItem
{
    private String testName;
    public String getTestName()
    {
        return testName;
    }

    private String resultName;
    public String getResultName()
    {
        return resultName;
    }

    private File sourceDir;
    public File getSourceDir()
    {
        return sourceDir;
    }

    private File zipFile;
    public File getZipFile()
    {
        return zipFile;
    }

    public ResultInfoItem(String testName, File sourceDir, File zipFile, String resultName)
    {
        this.testName = testName;
        this.resultName = resultName;
        this.sourceDir = sourceDir;
        this.zipFile = zipFile;
    }
}
