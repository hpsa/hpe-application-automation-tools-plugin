package com.hp.octane.integrations.dto.tests;

import com.hp.octane.integrations.dto.tests.TestRunError;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Created by gullery on 11/08/2016.
 */

@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.NONE)
public class TestRunErrorImpl implements TestRunError {

	@XmlAttribute(name = "type")
	private String errorType;

	@XmlAttribute(name = "message")
	private String errorMessage;

	@XmlValue
	private String stackTrace;

	public String getErrorType() {
		return errorType;
	}

	public TestRunError setErrorType(String errorType) {
		this.errorType = errorType;
		return this;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public TestRunError setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		return this;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public TestRunError setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
		return this;
	}
}
