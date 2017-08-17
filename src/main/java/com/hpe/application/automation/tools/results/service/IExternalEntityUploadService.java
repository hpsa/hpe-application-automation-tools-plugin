package com.hpe.application.automation.tools.results.service;

public interface IExternalEntityUploadService {

	public void UploadExternalTestSet(AlmRestInfo loginInfo, String reportFilePath, String testsetFolderPath, String testFolderPath, String testingFramework, String testingTool, String subversion, String jobName, String buildUrl) throws ExternalEntityUploadException;	

}
