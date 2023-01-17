/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.actions;

import com.hp.octane.integrations.uft.items.*;
import com.microfocus.application.automation.tools.octane.Messages;
import com.microfocus.application.automation.tools.octane.executor.UFTTestDetectionService;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible to show report of  {@link UFTTestDetectionService} for MBT
 */
public class UFTActionDetectionBuildAction implements Action {

    private AbstractBuild<?, ?> build;

    private UftTestDiscoveryResult results;

    private List<UftTestAction> actions;

    private Map<String, List<UftTestParameter>> actionToParametersMap;

    private List<UftTestParameter> parameters;

    public UFTActionDetectionBuildAction(final AbstractBuild<?, ?> build, UftTestDiscoveryResult results) {
        this.build = build;

        setResults(results);
    }

    private List<? extends SupportsOctaneStatus> getItemsWithStatus(List<? extends SupportsOctaneStatus> entities, OctaneStatus status) {
        return entities.stream()
                .filter(item -> status.equals(item.getOctaneStatus()))
                .collect(Collectors.toList());
    }

    private List<UftTestAction> flattenActions(UftTestDiscoveryResult results) {
        return results.getAllTests().stream()
                .map(AutomatedTest::getActions)
                .flatMap(Collection::stream)
                .filter(action -> !action.getOctaneStatus().equals(OctaneStatus.NONE))
                .collect(Collectors.toList());
    }

    private List<UftTestParameter> flattenParameters(List<UftTestAction> actions) {
        return actions.stream()
                .map(UftTestAction::getParameters)
                .flatMap(Collection::stream)
                .filter(parameter -> !parameter.getOctaneStatus().equals(OctaneStatus.NONE))
                .collect(Collectors.toList());
    }

    private Map<String, List<UftTestParameter>> setActionToParametersMap() {
        return actions.stream()
                .filter(action -> CollectionUtils.isNotEmpty(action.getParameters()))
                .collect(Collectors.toMap(UftTestAction::getRepositoryPath, UftTestAction::getParameters));
    }

    @Override
    public String getIconFileName() {
        return "notepad.png";
    }

    @Override
    public String getDisplayName() {
        return Messages.UFTActionDetectionBuildActionConfigurationLabel();
    }

    @Override
    public String getUrlName() {
        return "uft_report";
    }

    @SuppressWarnings("squid:S1452")
    public final Run<?, ?> getBuild() {
        return build;
    }

    public UftTestDiscoveryResult getResults() {
        return results;
    }

    public void setResults(UftTestDiscoveryResult results) {
        this.results = results;
        init(results);
    }

    public List<UftTestAction> getActions() {
        return actions;
    }

    public void setActions(List<UftTestAction> actions) {
        this.actions = actions;
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTActionDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasNewActions() {
        return getNewActions().size() > 0;
    }

    public List<UftTestAction> getNewActions() {
        return (List<UftTestAction>) getItemsWithStatus(actions, OctaneStatus.NEW);
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTActionDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasDeletedActions() {
        return getDeletedActions().size() > 0;
    }

    public List<UftTestAction> getDeletedActions() {
        return (List<UftTestAction>) getItemsWithStatus(actions, OctaneStatus.DELETED);
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTActionDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasUpdatedActions() {
        return getUpdatedActions().size() > 0;
    }

    public List<UftTestAction> getUpdatedActions() {
        return (List<UftTestAction>) getItemsWithStatus(actions, OctaneStatus.MODIFIED);
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTActionDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasNewParameters() {
        return getNewParameters().size() > 0;
    }

    public List<UftTestParameter> getNewParameters() {
        return (List<UftTestParameter>) getItemsWithStatus(parameters, OctaneStatus.NEW);
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTActionDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasDeletedParameters() {
        return getDeletedParameters().size() > 0;
    }

    public List<UftTestParameter> getDeletedParameters() {
        return (List<UftTestParameter>) getItemsWithStatus(parameters, OctaneStatus.DELETED);
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTActionDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasUpdatedParameters() {
        return getUpdatedParameters().size() > 0;
    }

    public List<UftTestParameter> getUpdatedParameters() {
        return (List<UftTestParameter>) getItemsWithStatus(parameters, OctaneStatus.MODIFIED);
    }

    public boolean getHasQuotedPaths() {
        return results.isHasQuotedPaths();
    }

    public Map<String, List<UftTestParameter>> getActionToParametersMap() {
        return actionToParametersMap;
    }

    private void init(UftTestDiscoveryResult results) {
        boolean isEmptyResults = results == null;
        if (isEmptyResults) {
            this.results = new UftTestDiscoveryResult();
            this.actions = Collections.emptyList();
            this.actionToParametersMap = Collections.emptyMap();
            this.parameters = Collections.emptyList();
        } else {
            this.actions = flattenActions(results);
            this.actionToParametersMap = setActionToParametersMap();
            this.parameters = flattenParameters(actions);
        }
    }

}
