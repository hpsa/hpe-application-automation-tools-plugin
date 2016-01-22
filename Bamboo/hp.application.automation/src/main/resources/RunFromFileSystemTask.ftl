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
.control,.helpIcon, .toolTip, .MCcheckBox{
    float:left;
}
.MCcheckBox{
    width:100%
}
.control,.helpIcon, .toolTip{
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
hr{
    clear:both;
    border:none;
}
.control{
    width:500px;
}
</style>
<div class="control">
    [@ww.textfield name="RunFromFileSystemTask.taskId" disabled="true"/]
</div>
<hr>
<div class="control">
    [@ww.textarea labelKey="RunFromFileSystemTaskConfigurator.testsPathInputLbl" name="testPathInput" required='true' rows="4"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromFileSystemTaskConfigurator.toolTip.tests');">?</div>
<div id ="RunFromFileSystemTaskConfigurator.toolTip.tests" class="toolTip">
    [@ww.text name='RunFromFileSystemTaskConfigurator.toolTip.tests'/]
</div>
<hr>
<div class="control">
    [@ww.textfield labelKey="RunFromFileSystemTaskConfigurator.timelineInputLbl" name="timeoutInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromFileSystemTaskConfigurator.toolTip.timeOut');">?</div>
<div id ="RunFromFileSystemTaskConfigurator.toolTip.timeOut" class="toolTip">
    [@ww.text name='RunFromFileSystemTaskConfigurator.toolTip.timeOut'/]
</div>
<hr>
<div class="control">
    [@ww.select labelKey="RunFromFileSystemTask.publishMode" name="publishMode" list="publishModeItems" emptyOption="false"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromFileSystemTaskConfigurator.toolTip.viewResults');">?</div>
<div id ="RunFromFileSystemTaskConfigurator.toolTip.viewResults" class="toolTip">
    [@ww.text name='RunFromFileSystemTaskConfigurator.toolTip.viewResults'/]
</div>
<hr>
<div class="MCcheckBox">
    [@ww.checkbox labelKey="RunFromFileSystemTaskConfigurator.toolTip.useMC" name="useMC" toggle='true'/]
</div>
[@ui.bambooSection dependsOn='useMC' showOn='true']
<div class="control">
[@ww.textfield labelKey="RunFromFileSystemTaskConfigurator.mcServerURLInputLbl" name="mcServerURLInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromFileSystemTaskConfigurator.toolTip.mcServerURL');">?</div>
<div id ="RunFromFileSystemTaskConfigurator.toolTip.mcServerURL" class="toolTip">
[@ww.text name='RunFromFileSystemTaskConfigurator.toolTip.mcServerURL'/]
</div>
<hr>
<div class="control">
[@ww.textfield labelKey="RunFromFileSystemTaskConfigurator.mcUserNameInputLbl" name="mcUserNameInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromFileSystemTaskConfigurator.toolTip.mcUserName');">?</div>
<div id ="RunFromFileSystemTaskConfigurator.toolTip.mcUserName" class="toolTip">
[@ww.text name='RunFromFileSystemTaskConfigurator.toolTip.mcUserName'/]
</div>
<hr>
<div class="control">
[@ww.password labelKey="RunFromFileSystemTaskConfigurator.mcPasswordInputLbl" name="mcPasswordInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromFileSystemTaskConfigurator.toolTip.mcPassword');">?</div>
<div id ="RunFromFileSystemTaskConfigurator.toolTip.mcPassword" class="toolTip">
[@ww.text name='RunFromFileSystemTaskConfigurator.toolTip.mcPassword'/]
</div>
<hr>
<div class="control">
[@ww.textfield labelKey="RunFromFileSystemTaskConfigurator.mcApplicationPathInputLbl" name="mcApplicationPathInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromFileSystemTaskConfigurator.toolTip.mcApplicationPath');">?</div>
<div id ="RunFromFileSystemTaskConfigurator.toolTip.mcApplicationPath" class="toolTip">
[@ww.text name='RunFromFileSystemTaskConfigurator.toolTip.mcApplicationPath'/]
</div>
<hr>
<div class="control">
[@ww.textfield labelKey="RunFromFileSystemTaskConfigurator.mcApplicationIDKeyInputLbl" name="mcApplicationIDKeyInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromFileSystemTaskConfigurator.toolTip.mcApplicationIDKey');">?</div>
<div id ="RunFromFileSystemTaskConfigurator.toolTip.mcApplicationIDKey" class="toolTip">
[@ww.text name='RunFromFileSystemTaskConfigurator.toolTip.mcApplicationIDKey'/]
</div>
<hr>
[/@ui.bambooSection]

<script  type="text/javascript">
    var customWidth = "500px";
    document.getElementById('timeoutInput').style.maxWidth=customWidth;
    document.getElementById('testPathInput').style.maxWidth=customWidth;
    document.getElementById('publishMode').style.maxWidth=customWidth;
    document.getElementById('mcServerURLInput').style.maxWidth=customWidth;
    document.getElementById('mcUserNameInput').style.maxWidth=customWidth;
    document.getElementById('mcPasswordInput').style.maxWidth=customWidth;
    document.getElementById('mcApplicationPathInput').style.maxWidth=customWidth;
    document.getElementById('mcApplicationIDKeyInput').style.maxWidth=customWidth;

    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if(e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }
</script>