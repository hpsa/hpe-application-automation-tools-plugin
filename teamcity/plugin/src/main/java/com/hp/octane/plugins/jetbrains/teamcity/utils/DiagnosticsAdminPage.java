package com.hp.octane.plugins.jetbrains.teamcity.utils;

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
public class DiagnosticsAdminPage extends AdminPage {
    public DiagnosticsAdminPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor) {
        super(pagePlaces);
        setPluginName("HPE NGA Settings");
        setIncludeUrl(descriptor.getPluginResourcesPath("settingsWebForm.jsp"));
        setTabTitle("HPE NGA Settings");
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
