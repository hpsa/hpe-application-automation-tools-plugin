/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.configuration;

import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * SDK based (log4j brought by SDK) logger provider
 * Main purpose of this custom logger provider is to ensure correct logs location configuration at the earliest point of the plugin initialization
 * TODO: this method might become a part of an SPI interface of SDK's plugin services
 */
public final class SDKBasedLoggerProvider {
	private static volatile boolean sysParamConfigured = false;

	private SDKBasedLoggerProvider(){
		//CodeClimate  : Add a private constructor to hide the implicit public one.
	}
	public static Logger getLogger(Class<?> type) {
		if (!sysParamConfigured) {
			System.setProperty("octaneAllowedStorage", CIJenkinsServicesImpl.getAllowedStorageFile().getAbsolutePath() + File.separator);
			sysParamConfigured = true;
		}
		return LogManager.getLogger(type);
	}
}
