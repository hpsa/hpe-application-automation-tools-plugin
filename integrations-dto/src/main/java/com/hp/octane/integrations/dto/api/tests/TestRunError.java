package com.hp.octane.integrations.dto.api.tests;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 11/08/2016.
 */

public interface TestRunError extends DTOBase {
	String getErrorType();

	TestRunError setErrorType(String errorType);

	String getErrorMessage();

	TestRunError setErrorMessage(String errorMsg);

	String getStackTrace();

	TestRunError setStackTrace(String stackTraceStr);
}
