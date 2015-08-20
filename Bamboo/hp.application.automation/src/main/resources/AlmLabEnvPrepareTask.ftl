[#macro newALMParam paramType ='ALMParamTypeManual' paramName='' paramValue=''
    tagNameType = "almParamTypes" tagNameName="almParamName" tagNameValue="almParamValue" chkFst= "almParamOnlyFirst"]
    <div class = "group" id = "ParamTemplate">
                [@ww.select labelKey="AlmLabEnvPrepareTask.Parameter.ParameterType" name=tagNameType
                        list=ALMParamsTypes listKey='key' listValue='value' value=paramType/]
                [@ww.textfield labelKey="AlmLabEnvPrepareTask.Parameter.ParameterName" name=tagNameName value=paramName/]
                [@ww.textfield labelKey="AlmLabEnvPrepareTask.Parameter.ParameterValue" name=tagNameValue value=paramValue/]

                [@ui.bambooSection dependsOn='almParamTypes' showOn='ALMParamTypeJson']
                    [@ww.checkbox name=chkFst labelKey="AlmLabEnvPrepareTask.Parameter.OnlyFirst" value=false toggle='true'/]
                [/@ui.bambooSection]
     <div>
[/#macro]

[@ww.textfield labelKey="AlmLabEnvPrepareTask.almServerInputLbl" name="almServer" required='true'/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.userNameInputLbl" name="almUserName" required='true' required='true'/]
[@ww.password labelKey="AlmLabEnvPrepareTask.passwordInputLbl" name="almUserPassword" showPassword="false"/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.domainInputLbl" name="domain" required='true'/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.projectInputLbl" name="almProject" required='true'/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.AUTEnvIDInputLbl" name="AUTEnvID" required='true'/]

<div class="field-group">
    [@ww.radio labelKey='AlmLabEnvPrepareTask.AUTEnvConfInputLbl' name='ALMConfigOptions'
            listKey='key' listValue='value' toggle='true'
            list=ALMConfigOptionsMap ]
     [/@ww.radio]

    [@ui.bambooSection dependsOn='ALMConfigOptions' showOn='ALMConfUseNew']
            [@ww.textfield labelKey="AlmLabEnvPrepareTask.createNewConfInputLbl" name="NewAUTConfName" required='true'/]
            [@ww.textfield labelKey="AlmLabEnvPrepareTask.assignAUTEnvConfIDtoInputLbl" name="outEnvID"/]
    [/@ui.bambooSection]
    [@ui.bambooSection dependsOn='ALMConfigOptions' showOn='ALMConfUseExist']
            [@ww.textfield labelKey="AlmLabEnvPrepareTask.useAnExistingConfInputLbl" name="AUTConfName" required='true'/]
    [/@ui.bambooSection]

</div>

[@ww.textfield labelKey="AlmLabEnvPrepareTask.pathToJSONFileInputLbl" name="pathToJSONFile"/]

<div class="field-group">
   <fieldset style="display: none;">
        [@newALMParam /]
   </fieldset>

    <table id="paramTable">
            [#list almParams as prm]
             <tr>
                  <td><input type="Button" class="Button" onclick="javascript: delRow(this)" value="Delete"></td>
                   <td>[@newALMParam paramType =prm.almParamSourceType paramName=prm.almParamName paramValue=prm.almParamValue/]</td>
             </tr>
            [/#list]
    </table>

    <div class="buttons-container">
        <div class="buttons">
            <button class="aui-button aui-button-primary" type="button" onclick="javascript: addNewALMParam()"> Add parameters </button>
        </div>
    </div>
</div>


<script  type="text/javascript">

           function addNewALMParam() {

               console.log("console.log");

               var divTemplate = document.getElementById('ParamTemplate');
               var table = document.getElementById('paramTable');

               var row = document.createElement("TR");
               var td1 = document.createElement("TD");
               var td2 = document.createElement("TD");

               var strHtml5 = "<INPUT TYPE=\"Button\" CLASS=\"Button\" onClick=\"javascript: delRow(this)\" VALUE=\"Delete\">";
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

</script>