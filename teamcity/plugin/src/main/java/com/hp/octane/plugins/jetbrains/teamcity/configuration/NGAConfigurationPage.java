package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by gadiel on 11/01/2016.
 */

public class NGAConfigurationPage extends AdminPage {

	protected NGAConfigurationPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor pluginDescriptor) {
		super(pagePlaces);
		setPluginName("HPE Lifecycle Management CI Settings");
		setTabTitle("HPE Lifecycle Management CI Settings");
		setIncludeUrl(pluginDescriptor.getPluginResourcesPath("settingsWebForm.jsp"));
		setPosition(PositionConstraint.after("clouds", "email", "jabber"));
		register();
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
	}

	@NotNull
	public String getGroup() {
		return SERVER_RELATED_GROUP;
	}
}
