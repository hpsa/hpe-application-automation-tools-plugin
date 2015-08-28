<style type="text/css">
.helpIcon{
    background-color: rgba(59, 115, 175, 1);
    color: white;
    width: 15px;
    border-radius:15px;
    font-weight: bold;
    padding-left:6px;
    cursor:pointer;
    margin:5px;
}
.control,.helpIcon, .toolTip, .CDAcheckBox{
    float:left;
}
.toolTip{
    display: none;
    border: solid #bbb 1px;
    background-color: #f0f0f0;
    padding: 1em;
    margin-bottom: 1em;
    width: 97%;
}
.CDAcheckBox{
    width:100%
}
</style>

[#-- @ftlvariable name="uiConfigBean" type="com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport" --]
<div class="toolTip" style="display: block; float: none;">[@ww.text name='AlmLabManagementTask.taskDescription'/]</div>
[@ww.textfield labelKey="AlmLabManagementTask.almServer" name="almServer" required='true'/]
[@ww.textfield labelKey="AlmLabManagementTask.userName" name="userName" required='true'/]
[@ww.textfield labelKey="AlmLabManagementTask.password" name="password" showPassword="false"/]
[@ww.textfield labelKey="AlmLabManagementTask.domain" name="domain" required='true'/]
[@ww.textfield labelKey="AlmLabManagementTask.projectName" name="projectName" required='true'/]
[@ww.select labelKey="AlmLabManagementTask.runType" name="runType" list="runTypeItems" emptyOption="false"/]
[@ww.textfield labelKey="AlmLabManagementTask.testId" name="testId" required='true'/]
[@ww.textfield labelKey="AlmLabManagementTask.description" name="description"/]
[@ww.textfield labelKey="AlmLabManagementTask.duration" name="duration" required='true'/]

<div class="control">
[@ww.textfield labelKey="AlmLabManagementTask.enviromentId" name="enviromentId"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('AlmLabManagementTask.toolTip.enviromentId');">?
</div>
<div id ="AlmLabManagementTask.toolTip.enviromentId" class="toolTip">
    [@ww.text name='AlmLabManagementTask.toolTip.enviromentId'/]
</div>

<br/>
<div class="CDAcheckBox">
[@ww.checkbox labelKey="AlmLabManagementTask.useSda" name="useSda" toggle='true'/]
</div>
[@ui.bambooSection dependsOn='useSda' showOn='true']
    [@ww.select labelKey="AlmLabManagementTask.deploymentAction" name="deploymentAction" list="deploymentActionItems"  emptyOption="false"/]
    [@ww.textfield labelKey="AlmLabManagementTask.deployedEnvironmentName" name="deployedEnvironmentName"/]
    [@ww.select labelKey="AlmLabManagementTask.deprovisioningAction" name="deprovisioningAction" list="deprovisioningActionItems" emptyOption="false"/]
[/@ui.bambooSection]

<script  type="text/javascript">
    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if(e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }
</script>