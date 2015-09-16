[@ww.textfield labelKey="Alm.almServerInputLbl" name="almServer" required='true'/]
[@ww.textfield labelKey="Alm.userNameInputLbl" name="userName" required='true'/]
[@ww.password labelKey="Alm.passwordInputLbl" name="password" showPassword="false"/]
[@ww.textfield labelKey="Alm.domainInputLbl" name="domain" required='true'/]
[@ww.textfield labelKey="Alm.projectInputLbl" name="projectName" required='true'/]
[@ww.textarea labelKey="Alm.testsPathInputLbl" name="testPathInput" required='true' rows="4"/]
[@ww.textfield labelKey="Alm.timelineInputLbl" name="timeoutInput"/]

[@ww.checkbox labelKey='Alm.advancedLbl' name='AdvancedOption' toggle='true' /]
[@ui.bambooSection dependsOn='AdvancedOption' showOn='true']
    [@ww.select labelKey="Alm.runModeInputLbl" name="runMode" list="runModeItems" emptyOption="false"/]
    [@ww.textfield labelKey="Alm.testingToolHostInputLbl" name="testingToolHost" required='false'/]
[/@ui.bambooSection]

<script  type="text/javascript">
    var customWidth = "500px";
    document.getElementById('almServer').style.maxWidth=customWidth;
    document.getElementById('userName').style.maxWidth=customWidth;
    document.getElementById('password').style.maxWidth=customWidth;
    document.getElementById('domain').style.maxWidth=customWidth;
    document.getElementById('projectName').style.maxWidth=customWidth;
    document.getElementById('testPathInput').style.maxWidth=customWidth;
    document.getElementById('timeoutInput').style.maxWidth=customWidth;
    document.getElementById('testingToolHost').style.maxWidth=customWidth;
    document.getElementById('runMode').style.maxWidth=customWidth;
</script>