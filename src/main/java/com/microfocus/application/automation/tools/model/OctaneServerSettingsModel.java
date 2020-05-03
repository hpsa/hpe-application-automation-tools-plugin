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

package com.microfocus.application.automation.tools.model;

import com.microfocus.application.automation.tools.octane.exceptions.AggregatedMessagesException;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.*;

/*
 * Model for sorting the Octane configuration
 */
public class OctaneServerSettingsModel {
	private String internalId = UUID.randomUUID().toString();

	private String identity;
	private Long identityFrom;

	private String uiLocation;
	private String username;
	private Secret password;
	private String impersonatedUser;
	private boolean suspend;
	private String sscBaseToken;

	// inferred from uiLocation
	private String location;
	private String sharedSpace;
	private long maxTimeoutHours;

	private String workspace2ImpersonatedUserConf;
	// inferred from workspace2ImpersonatedUserConf
	private Map<Long, String> workspace2ImpersonatedUserMap;

	public OctaneServerSettingsModel() {
	}

	@DataBoundConstructor
	public OctaneServerSettingsModel(String uiLocation, String username, Secret password, String impersonatedUser) {
		this.uiLocation = StringUtils.trim(uiLocation);
		this.username = username;
		this.password = password;
		this.impersonatedUser = impersonatedUser;
	}

	public String getInternalId() {
		return internalId;
	}

	public boolean isSuspend() {
		return this.suspend;
	}

	@DataBoundSetter
	public void setSuspend(boolean suspend) {
		this.suspend = suspend;
	}

	public String getSscBaseToken() {
		return this.sscBaseToken;
	}

	@DataBoundSetter
	public void setSscBaseToken(String sscBaseToken) {
		this.sscBaseToken = sscBaseToken;
	}

	public String getUiLocation() {
		return uiLocation;
	}

	public String getUsername() {
		return username;
	}

	public Secret getPassword() {
		return password;
	}

	public String getImpersonatedUser() {
		return impersonatedUser;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		if (StringUtils.isEmpty(identity)) {
			throw new IllegalArgumentException("Empty identity is not allowed");
		}
		this.identity = identity;
		this.setIdentityFrom(new Date().getTime());
	}

	public Long getIdentityFrom() {
		return identityFrom;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSharedSpace() {
		return sharedSpace;
	}

	public void setSharedSpace(String sharedSpace) {
		this.sharedSpace = sharedSpace;
	}

	public void setIdentityFrom(Long identityFrom) {
		this.identityFrom = identityFrom;
	}

	public long getMaxTimeoutHours() {
		return maxTimeoutHours;
	}

	public void setMaxTimeoutHours(long maxTimeoutHours) {
		this.maxTimeoutHours = maxTimeoutHours;
	}

	public boolean isValid() {
		return identity != null && !identity.isEmpty() &&
				location != null && !location.isEmpty() &&
				internalId != null && !internalId.isEmpty() &&
				sharedSpace != null && !sharedSpace.isEmpty();
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public String getCaption() {
		return getLocation() + "?p=" + getSharedSpace();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OctaneServerSettingsModel that = (OctaneServerSettingsModel) o;
		return suspend == that.suspend &&
				maxTimeoutHours == that.maxTimeoutHours &&
				Objects.equals(identity, that.identity) &&
				Objects.equals(username, that.username) &&
				Objects.equals(password, that.password) &&
				Objects.equals(impersonatedUser, that.impersonatedUser) &&
				Objects.equals(sscBaseToken, that.sscBaseToken) &&
				Objects.equals(location, that.location) &&
				Objects.equals(workspace2ImpersonatedUserConf, that.workspace2ImpersonatedUserConf) &&
				Objects.equals(sharedSpace, that.sharedSpace);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identity, username, password, impersonatedUser, suspend, sscBaseToken, location, sharedSpace, maxTimeoutHours, internalId, getWorkspace2ImpersonatedUserConf());
	}

	public String getWorkspace2ImpersonatedUserConf() {
		return workspace2ImpersonatedUserConf;
	}

	@DataBoundSetter
	public void setWorkspace2ImpersonatedUserConf(String workspace2ImpersonatedUserConf) {
		this.workspace2ImpersonatedUserConf = workspace2ImpersonatedUserConf;
		workspace2ImpersonatedUserMap = parseWorkspace2ImpersonatedUserConf(workspace2ImpersonatedUserConf, true);
	}

	public Map<Long, String> getWorkspace2ImpersonatedUserMap() {
		return workspace2ImpersonatedUserMap;
	}

	public static Map<Long, String> parseWorkspace2ImpersonatedUserConf(String workspace2ImpersonatedUserConf, boolean ignoreErrors) {
		Map<Long, String> workspace2ImpersonatedUserMap = new HashMap<>();
		List<String> errorsFound = new ArrayList<>();
		if (workspace2ImpersonatedUserConf != null) {
			try {
				String[] parts = workspace2ImpersonatedUserConf.split("[\\n;]");
				for (String part : parts) {
					String trimmedPart = part.trim();
					if (trimmedPart.isEmpty() || trimmedPart.startsWith("#")) {
						continue;
					}
					String[] subPart = part.split(":");
					if (subPart.length != 2) {
						errorsFound.add("Workspace configuration is not valid, valid format is 'Workspace ID:jenkins user': " + trimmedPart);
						continue;
					}

					long workspaceId;
					try {
						workspaceId = Long.parseLong(subPart[0].trim());
					} catch (NumberFormatException e) {
						errorsFound.add("Workspace configuration is not valid, workspace ID must be numeric: " + trimmedPart);
						continue;
					}
					String user = subPart[1].trim();
					if (user.isEmpty()) {
						errorsFound.add("Workspace configuration is not valid, user value is empty: " + trimmedPart);
						continue;
					}

					if (workspace2ImpersonatedUserMap.containsKey(workspaceId)) {
						errorsFound.add("Duplicated workspace configuration: " + trimmedPart);
						continue;
					}
					workspace2ImpersonatedUserMap.put(workspaceId, user);

				}
			} catch (Exception e) {
				errorsFound.add("Unexpected exception during workspace configuratin parsing: " + e.getMessage());
			}

		}
		if (!ignoreErrors && !errorsFound.isEmpty()) {
			throw new AggregatedMessagesException(errorsFound);
		}
		return workspace2ImpersonatedUserMap;
	}
}
