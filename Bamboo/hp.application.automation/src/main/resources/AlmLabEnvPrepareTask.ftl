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
.control,.helpIcon, .toolTip, .parameterWrapper, #paramTable{
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
#paramTable{
    width:100%;
}
</style>

[#macro newALMParam paramType ='ALMParamTypeManual' paramName='' paramValue=''
    tagNameType = "almParamTypes" tagNameName="almParamName" tagNameValue="almParamValue" chkFst= "almParamOnlyFirst"]
    <div id = "ParamTemplate">
        [@ww.select labelKey="AlmLabEnvPrepareTask.Parameter.ParameterType" name=tagNameType
            list=ALMParamsTypes listKey='key' listValue='value' value=paramType descriptionKey="AlmLabEnvPrepareTask.toolTip.parameterType"/]
        [@ww.textfield labelKey="AlmLabEnvPrepareTask.Parameter.ParameterName" name=tagNameName value=paramName descriptionKey="AlmLabEnvPrepareTask.toolTip.parameterName"/]
        [@ww.textfield labelKey="AlmLabEnvPrepareTask.Parameter.ParameterValue" name=tagNameValue value=paramValue descriptionKey="AlmLabEnvPrepareTask.toolTip.parameterValue"/]
        [@ui.bambooSection dependsOn='almParamTypes' showOn='ALMParamTypeJson']
            [@ww.checkbox name=chkFst labelKey="AlmLabEnvPrepareTask.Parameter.OnlyFirst" value=false toggle='true'/]
        [/@ui.bambooSection]
    </div>
[/#macro]

<div class="toolTip" style="display: block; float: none;">[@ww.text name='AlmLabEnvPrepareTask.taskDescription'/]</div>
[@ww.textfield labelKey="AlmLabEnvPrepareTask.almServerInputLbl" name="almServer" required='true'/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.userNameInputLbl" name="almUserName" required='true' required='true'/]
[@ww.password labelKey="AlmLabEnvPrepareTask.passwordInputLbl" name="almUserPassword" showPassword="false"/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.domainInputLbl" name="domain" required='true'/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.projectInputLbl" name="almProject" required='true'/]
<div class="control">
    [@ww.textfield labelKey="AlmLabEnvPrepareTask.AUTEnvIDInputLbl" name="AUTEnvID" required='true'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('AlmLabEnvPrepareTask.toolTip.AUTEnvID');">?</div>
<div id ="AlmLabEnvPrepareTask.toolTip.AUTEnvID" class="toolTip">
    [@ww.text name='AlmLabEnvPrepareTask.toolTip.AUTEnvID'/]
</div>
<div>
[@ww.radio labelKey='AlmLabEnvPrepareTask.AUTEnvConfInputLbl' name='ALMConfigOptions'
        listKey='key' listValue='value' toggle='true'
        list=ALMConfigOptionsMap ]
 [/@ww.radio]

[@ui.bambooSection dependsOn='ALMConfigOptions' showOn='ALMConfUseNew']
        [@ww.textfield labelKey="AlmLabEnvPrepareTask.createNewConfInputLbl" name="NewAUTConfName" required='true'/]
        <div class="control">
            [@ww.textfield labelKey="AlmLabEnvPrepareTask.assignAUTEnvConfIDtoInputLbl" name="outEnvID"/]
        </div>
        <div class="helpIcon" onclick="javascript: toggle_visibility('AlmLabEnvPrepareTask.toolTip.AUTEnvConfID');">?</div>
        <div id ="AlmLabEnvPrepareTask.toolTip.AUTEnvConfID" class="toolTip">
            [@ww.text name='AlmLabEnvPrepareTask.toolTip.AUTEnvConfID'/]
        </div>
[/@ui.bambooSection]
[@ui.bambooSection dependsOn='ALMConfigOptions' showOn='ALMConfUseExist']
        [@ww.textfield labelKey="AlmLabEnvPrepareTask.useAnExistingConfInputLbl" name="AUTConfName" required='true'/]
[/@ui.bambooSection]

</div>

<div class="control">
    [@ww.textfield labelKey="AlmLabEnvPrepareTask.pathToJSONFileInputLbl" name="pathToJSONFile"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('AlmLabEnvPrepareTask.toolTip.JSONPath');">?</div>
<div id ="AlmLabEnvPrepareTask.toolTip.JSONPath" class="toolTip">
    [@ww.text name='AlmLabEnvPrepareTask.toolTip.JSONPath'/]
</div>

<fieldset style="display: none;">
    [@newALMParam /]
</fieldset>

<table id="paramTable">
    [#list almParams as prm]
     <tr>
          <td><input type="Button" class="Button" onclick="javascript: delRow(this)" value="[@ww.text name='AlmLabEnvPrepareTask.btn.Delete'/]"></td>
          <td>[@newALMParam paramType =prm.almParamSourceType paramName=prm.almParamName paramValue=prm.almParamValue/]</td>
     </tr>
    [/#list]
</table>

<div class="buttons-container">
    <div class="buttons">
        <button class="aui-button aui-button-primary" type="button" onclick="javascript: addNewALMParam()">
            [@ww.text name='AlmLabEnvPrepareTask.btn.AddParameters'/]
        </button>
    </div>
</div>

<script  type="text/javascript">
           function addNewALMParam() {
               var divTemplate = document.getElementById('ParamTemplate');
               var table = document.getElementById('paramTable');

               var row = document.createElement("TR");
               var td1 = document.createElement("TD");
               var td2 = document.createElement("TD");

               var strHtml5 = "<INPUT TYPE=\"Button\" CLASS=\"Button\" onClick=\"javascript: delRow(this)\" VALUE=\"[@ww.text name='AlmLabEnvPrepareTask.btn.Delete'/]\">";
               td1.innerHTML = strHtml5;

               var divClone = divTemplate.cloneNode(true);
               td2.appendChild(divClone);

               row.appendChild(td1);
               row.appendChild(td2);

               table.appendChild(row);
           }

           function delRow(tableID) {
              var current = tableID;
               while ( (current = current.parentElement)  && current.tagName !="TR");
                       current.parentElement.removeChild(current);
           }

           function toggle_visibility(id) {
               var e = document.getElementById(id);
               if(e.style.display == 'block')
                   e.style.display = 'none';
               else
                   e.style.display = 'block';
           }
</script>