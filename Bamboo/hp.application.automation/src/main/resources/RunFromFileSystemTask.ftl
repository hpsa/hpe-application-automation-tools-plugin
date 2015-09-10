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
</style>
<div class="control">
    [@ww.textfield name="RunFromFileSystemTaskConfigurator.taskId" disabled="true"/]
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

<script  type="text/javascript">
    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if(e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }
</script>