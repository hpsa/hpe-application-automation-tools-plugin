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
    .helpIcon {
        background-color: rgba(59, 115, 175, 1);
        color: white;
        width: 15px;
        border-radius: 15px;
        font-weight: bold;
        padding-left: 6px;
        cursor: pointer;
        margin: 5px;
    }

    .control, .helpIcon, .toolTip, .MCcheckBox, .parameterWrapper, #paramTable {
        float: left;
    }

    #paramTable {
        width: 100%;
    }

    .MCcheckBox {
        width: 100%
    }

    .control, .helpIcon, .toolTip {
        float: left;
    }

    .toolTip {
        display: none;
        border: solid #bbb 1px;
        background-color: #f0f0f0;
        padding: 1em;
        margin-bottom: 1em;
        width: 97%;
    }

    hr {
        clear: both;
        border: none;
    }

    .control {
        width: 500px;
    }

    form.aui .field-group input.text {
        max-width: 500px;
    }

    h3.title {
        margin: 0px;
    }

    #extraApps {
        min-height: 30px;
        border: 1px solid #ccc;
        border-radius: 1px;
    }

    .extra-app-info {
        padding: 8px 0px;
    }

    .extra-app-info .app-name {
        margin-right: 10px;
    }
</style>

[#macro newMCParam paramValue='' tagNameName="mcAppPath"]
<div id="ParamTemplate">
    [@ww.textfield labelKey="Application Path" name=tagNameName value=paramValue/]
</div>
[/#macro]

<div class="control">
[@ww.textfield name="UploadApplicationTask.taskId" disabled="true"/]
</div>
<hr>

<div class="control">
[@ww.textfield labelKey="MC URL" name="mcServerURLInput"/]
</div>
<hr>
<div class="control">
[@ww.textfield labelKey="MC UserName" name="mcUserNameInput"/]
</div>
<hr>
<div class="control">
[@ww.password labelKey="MC Password" name="mcPasswordInput"/]
</div>
<hr>

<div class="MCcheckBox">
[@ww.checkbox labelKey="Use Proxy" name="useProxy" toggle='true'/]
</div>
[@ui.bambooSection dependsOn='useProxy' showOn='true']
<hr>
<div class="control">
    [@ww.textfield labelKey="Proxy Address" name="proxyAddress" /]
</div>
<hr>

<div class="MCcheckBox">
    [@ww.checkbox labelKey="Specify Authentication" name="specifyAuthentication" toggle="true"/]
</div>
<hr>
<div class="control">
    [@ww.textfield labelKey="Proxy Username" name="proxyUserName" disabled="true" /]
</div>
<hr>
<div class="control">
    [@ww.password labelKey="Proxy Password" name="proxyPassword" disabled="true" /]
</div>
<hr>
[/@ui.bambooSection]

<!--test table element to add and delete parameter -->
<fieldset style="display: none;">
[@newMCParam /]
</fieldset>

<table id="paramTable">
[#list mcPathParams as prmVal]
    <tr>
        <td width="100px" style="padding-top: 20px;"><input type="Button" class="aui-button aui-button-primary" onclick="javascript:delRow(this)"
                                 value="[@ww.text name='Delete'/]"</td>
        <td>[@newMCParam paramValue=prmVal /]</td>
    </tr>
[/#list]
</table>

<div class="buttons-container">
    <div class="buttons">
        <button class="aui-button aui-button-primary" type="button" onclick="javascript: addnewMCParam()">
        [@ww.text name='Add Applications'/]
        </button>
    </div>
</div>
<hr>


<script>
    var specifyAuthenticationBox = document.getElementById('specifyAuthentication');
    specifyAuthenticationBox.addEventListener('change', function(e) {
        var proxyUserNameInput = document.getElementById('proxyUserName'),
                proxyPasswordInput = document.getElementById('proxyPassword');

        if (specifyAuthenticationBox.checked == true) {
            proxyUserNameInput.disabled = false;
            proxyPasswordInput.disabled = false;
        } else {
            proxyUserNameInput.disabled = true;
            proxyPasswordInput.disabled = true;
        }
    });
    function addnewMCParam() {
        var divTemplate = document.getElementById('ParamTemplate');
        var table = document.getElementById('paramTable');

        var row = document.createElement("TR");
        var td1 = document.createElement("TD");
        var td2 = document.createElement("TD");

        var strHtml5 = "<INPUT TYPE=\"Button\" CLASS=\"aui-button aui-button-primary\" onClick=\"javascript: delRow(this)\" VALUE=\"[@ww.text name='Delete'/]\">";
        td1.innerHTML = strHtml5;
        td1.width = '100px';
        td1.style.paddingTop = '20px';

        var divClone = divTemplate.cloneNode(true);
        td2.appendChild(divClone);

        row.appendChild(td1);
        row.appendChild(td2);

        table.appendChild(row);
    }

    function delRow(tableID) {
        var current = tableID;
        while ((current = current.parentElement) && current.tagName != "TR");
        current.parentElement.removeChild(current);
    }
</script>