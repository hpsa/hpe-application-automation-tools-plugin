/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

/**
 * Created by dsinelnikov on 8/14/2015.
 */
package com.hpe.application.automation.bamboo.tasks;

import java.io.File;

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
