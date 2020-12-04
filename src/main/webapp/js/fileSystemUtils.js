/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */
document.addEventListener('DOMContentLoaded', function() {
    var checkbox1 = document.getElementById('checkBox1');

    if(checkbox1.checked){
         document.getElementsByName("fsTestType")[0].disabled = false;
         document.getElementsByName("selectedNode")[0].disabled = false;

    }else{
        document.getElementsByName("fsTestType")[0].disabled = true;
        document.getElementsByName("selectedNode")[0].disabled = true;
    }



    var selectIndex = document.getElementsByName("fsTestType")[0].selectedIndex;
    var selectValue = document.getElementsByName("fsTestType")[0].options[selectIndex].text;
    if(selectValue === "Rerun the entire set of tests" || selectValue === "Rerun only failed tests") {
        selectCleanupTest("none");
    } else {
        selectCleanupTest("block");
    }

}, false);

function useAuthentication(obj){
    if(obj.checked){
        document.getElementsByName("runfromfs.fsProxyUserName")[0].disabled=null;
        document.getElementsByName("runfromfs.fsProxyPassword")[0].disabled=null;
    }else{
        document.getElementsByName("runfromfs.fsProxyUserName")[0].disabled="true";
        document.getElementsByName("runfromfs.fsProxyPassword")[0].disabled="true";
    }
}

function enableCombobox(object){
    if (object.checked){
        document.getElementsByName("fsTestType")[0].disabled = false;
        document.getElementsByName("selectedNode")[0].disabled = false;
        document.getElementById("checkBox2").disabled=false;
    } else {
        document.getElementsByName("fsTestType")[0].disabled = true;
        document.getElementsByName("selectedNode")[0].disabled = true;
        document.getElementById("checkBox2").disabled=true;
    }
}

function fileSelected(input){
    var selectIndex = document.getElementById('testTypeSelect').selectedIndex;
    var selectValue = document.getElementById('testTypeSelect').options[selectIndex].text;
    if(selectValue === "Rerun the entire set of tests" || selectValue === "Rerun only failed tests") {
        document.getElementsByName("uftSettingsModel.cleanupTest")[0].value = input.files[0].name;
    } else {
        addCleanupTest(input.files[0].name);
    }
}

function selectCleanupTest(displayStyle) {
    document.getElementById('clearBtn').style.display = displayStyle;
    document.getElementById('clear').style.display = displayStyle;
    document.getElementById('copyPasteBtn').style.display = displayStyle;
    document.getElementById('infoMessage').style.display = displayStyle;
    document.getElementById('testsTable').style.display = displayStyle;
}

function selectValueCombo(selectObj) {

    var selectIndex = selectObj.selectedIndex;
    var selectValue = selectObj.options[selectIndex].text;

    if (selectValue === "Rerun the entire set of tests" || selectValue === "Rerun only failed tests") {
        selectCleanupTest("none");
    } else {
        selectCleanupTest("block");

        if (selectValue === "Of any of the build's tests") {
            selectCleanupTest("hidden");
            document.getElementById("checkBox2").disabled = false;
        } else {
            selectCleanupTest("visible");
            document.getElementById("checkBox2").disabled = true;
        }
    }

    function copyPasteRerunSettings() {
        var checkedTests = document.getElementsByName("rerunSettingsModels.checked");
        var rerunsList = document.getElementsByName('rerunSettingsModels.numberOfReruns');
        var cleanupTestList = document.getElementsByName('rerunSettingsModels.cleanupTest');
        var index = 0;

        var cleanupTest = document.getElementsByName("uftSettingsModel.cleanupTest")[0].value;
        var numberOfReruns = document.getElementsByName("numberOfReruns")[0].value;

        rerunsList.forEach(function (element) {
            if (checkedTests[index].checked) {
                element.value = numberOfReruns;
            }
            index = index + 1;
        });

        index = 0;
        cleanupTestList.forEach(function (element) {
            if (checkedTests[index].checked) {
                element.value = cleanupTest;
            }
            index = index + 1;
        });
    }


    function addCleanupTest(cleanupTest) {
        var selectCleanupLists = document.getElementsByName("rerunSettingsModels.cleanupTests");

        selectCleanupLists.forEach(function (element) {
            var option = document.createElement("option");
            option.text = cleanupTest;
            option.value = cleanupTest;
            element.add(option);
        });
    }

    function clearRerunSettings() {
        alert("clear");
        var checkBoxes = document.getElementsByName("rerunSettingsModels.checked");
        checkBoxes.forEach(function (element) {
            element.checked = false;
        });

        var numberOfRerunsFields = document.getElementsByName("rerunSettingsModels.numberOfReruns");
        numberOfRerunsFields.forEach(function (element) {
            element.value = 0;
        });

        var selectCleanupLists = document.getElementsByName("rerunSettingsModels.cleanupTest");
        selectCleanupLists.forEach(function (element) {
            element.value = "";
        });
    }
}



