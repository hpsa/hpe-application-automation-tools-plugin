package com.hpe.application.automation.tools.octane.actions;

import hudson.Extension;
import hudson.security.csrf.CrumbExclusion;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 * this class allows webhook resource to be excluded from CSRF validations
 * in case jenkins configured to have this kind of validation
 */
@Extension
public class SonarQubeWebHookCrumbExclusion extends CrumbExclusion {

	@Override
	public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
		String pathInfo = req.getPathInfo();
		if (isEmpty(pathInfo)) {
			return false;
		}
		if (!pathInfo.equals(getExclusionPath())) {
			return false;
		}
		chain.doFilter(req, resp);
		return true;
	}

	public String getExclusionPath() {
		return "/" + Webhooks.WEBHOOK_PATH + Webhooks.NOTIFY_METHOD;
	}
}