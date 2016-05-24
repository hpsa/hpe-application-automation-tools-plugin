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
    .control,.helpIcon, .toolTip, .MCcheckBox, .parameterWrapper, #paramTable {
        float:left;
    }
    #paramTable{
        width:100%;
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
<div class="control">
    [@ww.textfield name="RunFromFileSystemTask.taskId" disabled="true"/]
</div>
<hr>
<div class="control">
[@ww.textarea labelKey="RunFromFileSystemTaskConfigurator.testsPathInputLbl" id="testPathInput" name="testPathInput" required='true' rows="4"/]
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
<div class="btn-container">
    <button class="action-button" id="openMCBtn" onclick="javascript: openMCWizardHandler(event);">Open Wizard</button>
</div>
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
<div class="MCcheckBox">
    [@ww.checkbox labelKey="Use SSL" name="useSSL" toggle='false'/]
</div>
<hr>

<div class="MCcheckBox">
    [@ww.checkbox labelKey="Use Proxy" name="useProxy" toggle='true'/]
</div>
<hr>
    [@ui.bambooSection dependsOn='useProxy' showOn='true']
    <div class="control">
        [@ww.textfield labelKey="Proxy Address" name="proxyAddress"/]
    </div>
    <hr>
    <div class="MCcheckBox">
        [@ww.checkbox labelKey="Specify Autheration" name="specifyAutheration" toggle='true'/]
    </div>
    <hr>
    <div class="control">
        [@ww.textfield labelKey="Proxy Username" name="proxyUserName" disabled="true" /]
    </div>
    <hr>
    <div class="control">
        [@ww.password labelKey="Proxy Password" name="proxyPassword" disabled="true"/]
    </div>
    <hr>
    [/@ui.bambooSection]
<h3 class="title" id="deviceCapability">Device Information</h3>
<!-- <input type="hidden" id="jobUUID" name="jobUUID"/> -->
    [@ww.hidden id="jobUUID" name="jobUUID"/]
<div class="control">
    [@ww.textfield labelKey="Device Id" name="deviceId" readonly="true" /]
</div>
<hr>

<div class="control">
    [@ww.textfield labelKey="OS" name="OS" readonly="true" /]
</div>
<hr>

<div class="control">
    [@ww.textfield labelKey="Manufacturer And Model" name="manufacturerAndModel" readonly="true" /]
</div>
<hr>
<div class="control">
    [@ww.textfield labelKey="Target Lab" name="targetLab" readonly="true" /]
</div>
<hr>
<!-- <h3 class="title">Application Under Test</h3> -->
<div class="control">
    [@ww.textarea labelKey="Application Under Test" id="extraApps" name="extraApps" rows="4" readonly="true"/]
</div>
<hr>

<h3 class="title">Test Definitions</h3>
<div class="control">
    [@ww.textfield labelKey="Launch On Start" name="launchApplicationName" readonly="true" /]
</div>
<hr>

<div class="control">
    [@ww.textfield labelKey="AUT Actions" name="autActions" readonly="true" /]
</div>
<hr>

<div class="control">
    [@ww.textfield labelKey="AUT Packaging" name="instrumented" readonly="true" /]
</div>
<hr>
<div class="control">
    [@ww.textfield labelKey="Device Metrics" name="deviceMetrics" readonly="true" /]
</div>
<hr>
[/@ui.bambooSection]

<script  type="text/javascript">
var jobId,
        wizard,
        loginInfo,
        mcServerURLInput,
        mcUserNameInput,
        mcPasswordInput,
        useProxy,
        proxyAddress,
        proxyUserName,
        proxyPassword;
    var customWidth = "500px";
    document.getElementById('timeoutInput').style.maxWidth=customWidth;
    document.getElementById('testPathInput').style.maxWidth=customWidth;
    document.getElementById('publishMode').style.maxWidth=customWidth;
    document.getElementById('mcServerURLInput').style.maxWidth=customWidth;
    document.getElementById('mcUserNameInput').style.maxWidth=customWidth;
    document.getElementById('mcPasswordInput').style.maxWidth=customWidth;
document.getElementById('extraApps').style.maxWidth=customWidth;
var openMCBtn = document.getElementById('openMCBtn');
var specifyAutherationBox = document.getElementById('specifyAutheration');
specifyAutherationBox.addEventListener('change', function(e) {
    var proxyUserNameInput = document.getElementById('proxyUserName'),
            proxyPasswordInput = document.getElementById('proxyPassword');

    if (specifyAutherationBox.checked == true) {
        proxyUserNameInput.disabled = false;
        proxyPasswordInput.disabled = false;
    } else {
        proxyUserNameInput.disabled = true;
        proxyPasswordInput.disabled = true;
    }
});
    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if(e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block'
}
function openMCWizardHandler(e) {
    //disable open wizard button
    openMCBtn.disabled = true;

    //get login info, url, username, password
    mcServerURLInput = document.getElementById('mcServerURLInput').value;
    mcUserNameInput = document.getElementById('mcUserNameInput').value;
    mcPasswordInput = document.getElementById('mcPasswordInput').value;

    if (!mcServerURLInput || !mcUserNameInput || !mcPasswordInput) {
        alert('Mobile Center URL, Username, Password cannot be empty.');
        openMCBtn.disabled = false;
        return;
    } else {
        loginInfo = {
            mcServerURLInput: mcServerURLInput,
            mcUserNameInput: mcUserNameInput,
            mcPasswordInput: mcPasswordInput
        };
    }

    if (document.getElementById('useProxy').checked) {
        proxyAddress = document.getElementById('proxyAddress').value;
        proxyUserName = document.getElementById('proxyUserName').value;
        proxyPassword = document.getElementById('proxyPassword').value;

        loginInfo = {
            mcServerURLInput: mcServerURLInput,
            mcUserNameInput: mcUserNameInput,
            mcPasswordInput: mcPasswordInput,
            proxyAddress: proxyAddress,
            proxyUserName: proxyUserName,
            proxyPassword: proxyPassword
        };
    }
    //no need do login, get job id directly
    getJobIdHelper();
}
function getJobIdHelper() {
    var jobIdInput = document.getElementById("jobUUID");

    AJS.$.ajax({
        url: "${req.contextPath}/plugins/servlet/httpOperationServlet?method=createTempJob",
        type: "POST",
        data: loginInfo,
        dataType: "json",
        success: function(data) {
            //data = JSON.parse(data);
            if(data != null){
                jobId =  data.data && data.data.id;

                if (!jobId){
                    alert('Login to Mobile Center failed, mc login information is incorrect.');
                    return;
                }

                //set jobId to hidden input
                jobIdInput.value = jobId;
                //open MC wizard
                wizard = window.open(
                        mcServerURLInput+ "/integration/#/login?jobId=" + jobId + "&displayUFTMode=true&appType=native",
                        "MCWizardWindow",
                        "width=1024,height=768");
                wizard.focus();
                window.addEventListener('message', messageEventHandler, false);
            }else{
                alert('Login to Mobile Center failed, mc login information is incorrect.');
            }


        },
        error: function(error) {
            alert('Login to Mobile Center failed, please contact your administrator.');
            openMCBtn.disabled = false;
        }
    });
}
function messageEventHandler(event) {
    var me = this;
    //stop event bubble
    event.stopPropagation();
    console.log("===message event listener called from bamboo=====", event.data);

    if (event && event.data == "mcJobUpdated") {
        console.log("=====get device and application from mc success====", loginInfo);
        //get device and application
        AJS.$.ajax({
            url: "${req.contextPath}/plugins/servlet/httpOperationServlet?method=getJobJSONData&jobUUID=" + jobId,
            type: "POST",
            data: loginInfo,
            dataType: "json",
            success: function(data) {
                //data = JSON.parse(data);
                console.log("=====get device and application from mc success====", data);
                //set device and application information to test
                me._parseTestInfoHelper(data);
                //enable action button after the wizard closed
                openMCBtn.disabled = false;

                wizard.close();
            },
            error: function(error) {
                console.log("=====get job detail from mc fail====");
                alert('Get job detail from Mobile Center failed, please try again.');
                //enable action button after the wizard closed
                openMCBtn.disabled = false;
            }
        });
    }

    if (event && event.data === 'mcCloseWizard') {
        wizard.close();
        //enable action button after the wizard closed
        openMCBtn.disabled = false;
    }
}

function checkWizardStatus() {
    if (wizard && wizard.closed) {
        clearInterval(timer);
        //enable action button after the wizard closed
        openMCBtn.disabled = false;
    }
}
var timer = setInterval(checkWizardStatus, 500);

function _parseTestInfoHelper(testData) {
    delete testData.jobUUID;
    //render extra apps first
    _extraAppsReader(testData.extraApps || []);

    //delete testData.extraApps;

    for (var key in testData) {
        if (key == 'extraApps') continue;

        for (var infoKey in testData[key]) {
            if (document.getElementById(infoKey) != null) {
                document.getElementById(infoKey).value = testData[key][infoKey];
            }
        }
    }

    //deviceCapability and specificDevice cannot exist at the same time
    if (testData.specificDevice.deviceId) {
        document.getElementById('targetLab').value = '';
    } else {
        document.getElementById('deviceId').value = '';
    }

    return false;
}
function _extraAppsReader(extraApps) {
    console.log('=====extraApps=====', extraApps);
    var extraAppsContainer = document.getElementById("extraApps");
    var extraAppsInfo = '';

    //remove all children before add new
    //extraAppsContainer.innerHTML = '';
    extraApps.forEach(function(app, index, array) {
        // extraAppsContainer.appendChild(appContainer);
        extraAppsInfo += app.extraAppName + ': ' + app.instrumented + '\n';
    });

    extraAppsContainer.value = extraAppsInfo;

    return false;
}
</script>