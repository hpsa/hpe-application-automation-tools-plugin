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