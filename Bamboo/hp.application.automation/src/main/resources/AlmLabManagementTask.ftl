[#-- © Copyright 2015 Hewlett Packard Enterprise Development LP--]
[#--                                                                            --]
[#-- Permission is hereby granted, free of charge, to any person obtaining a copy--]
[#-- of this software and associated documentation files (the "Software"), to deal--]
[#-- in the Software without restriction, including without limitation the rights--]
[#-- to use, copy, modify, merge, publish, distribute, sublicense, and/or sell--]
[#-- copies of the Software, and to permit persons to whom the Software is-->]
[#-- furnished to do so, subject to the following conditions:--]
[#--                                                                            --]
[#-- The above copyright notice and this permission notice shall be included in--]
[#-- all copies or substantial portions of the Software.--]
[#--                                                                            --]
[#-- THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR--]
[#-- IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,--]
[#-- FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE--]
[#-- AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER--]
[#-- LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,--]
[#-- OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN--]
[#-- THE SOFTWARE.--]

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
.control{
    width:500px;
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
    [@ww.textfield labelKey="AlmLabManagementTask.environmentId" name="environmentId"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('AlmLabManagementTask.toolTip.environmentId');">?</div>
<div id ="AlmLabManagementTask.toolTip.environmentId" class="toolTip">
    [@ww.text name='AlmLabManagementTask.toolTip.environmentId'/]
</div>

<div class="CDAcheckBox">
    [@ww.checkbox labelKey="AlmLabManagementTask.useSda" name="useSda" toggle='true'/]
</div>
[@ui.bambooSection dependsOn='useSda' showOn='true']
    [@ww.select labelKey="AlmLabManagementTask.deploymentAction" name="deploymentAction" list="deploymentActionItems"  emptyOption="false"/]
    [@ww.textfield labelKey="AlmLabManagementTask.deployedEnvironmentName" name="deployedEnvironmentName"/]
    [@ww.select labelKey="AlmLabManagementTask.deprovisioningAction" name="deprovisioningAction" list="deprovisioningActionItems" emptyOption="false"/]
[/@ui.bambooSection]

<script  type="text/javascript">
    var customWidth = "500px";
    document.getElementById('almServer').style.maxWidth=customWidth;
    document.getElementById('userName').style.maxWidth=customWidth;
    document.getElementById('password').style.maxWidth=customWidth;
    document.getElementById('domain').style.maxWidth=customWidth;
    document.getElementById('projectName').style.maxWidth=customWidth;
    document.getElementById('runType').style.maxWidth=customWidth;
    document.getElementById('testId').style.maxWidth=customWidth;
    document.getElementById('description').style.maxWidth=customWidth;
    document.getElementById('duration').style.maxWidth=customWidth;
    document.getElementById('environmentId').style.maxWidth=customWidth;
    document.getElementById('deployedEnvironmentName').style.maxWidth=customWidth;
    document.getElementById('deploymentAction').style.maxWidth=customWidth;
    document.getElementById('deprovisioningAction').style.maxWidth=customWidth;

    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if(e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }
</script>