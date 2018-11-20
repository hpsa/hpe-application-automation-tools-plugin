/*
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

document.addEventListener('DOMContentLoaded', function() {
    var checkbox = document.getElementById('checkBox1');
    if(checkbox.checked){
         document.getElementsByName("fsTestType")[0].disabled = false;
         document.getElementsByName("selectedNode")[0].disabled = false;

    }else{
        document.getElementsByName("fsTestType")[0].disabled = true;
        document.getElementsByName("selectedNode")[0].disabled = true;
    }


    var selectIndex = document.getElementsByName("fsTestType")[0].selectedIndex;
    var selectValue = document.getElementsByName("fsTestType")[0].options[selectIndex].text;
    if(selectValue === "Of any of the build's tests") {
        document.getElementById('testsTable').style.visibility = "hidden";
        document.getElementById('clearBtn').style.visibility = "hidden";
        document.getElementById('copyPasteBtn').style.visibility = "hidden";
        document.getElementById('clear').style.visibility = "hidden";
        document.getElementById('infoMessage').style.visibility = "hidden";
    } else {
        document.getElementById('testsTable').style.visibility = "visible";
        document.getElementById('clearBtn').style.visibility = "visible";
        document.getElementById('copyPasteBtn').style.visibility = "visible";
        document.getElementById('clear').style.visibility = "visible";
        document.getElementById('infoMessage').style.visibility = "visible";
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
    } else {
        document.getElementsByName("fsTestType")[0].disabled = true;
        document.getElementsByName("selectedNode")[0].disabled = true;
    }
}

function fileSelected(input){
    var selectIndex = document.getElementById('testTypeSelect').selectedIndex;
    var selectValue = document.getElementById('testTypeSelect').options[selectIndex].text;
    if(selectValue === "Of any of the build's tests") {
        document.getElementsByName("uftSettingsModel.cleanupTest")[0].value = input.files[0].name;
    } else {
        addCleanupTest(input.files[0].name);
    }
}

function selectCleanupTest(displayStyle) {
    document.getElementById('clearBtn').style.visibility = displayStyle;
    document.getElementById('clear').style.visibility = displayStyle;
    document.getElementById('copyPasteBtn').style.visibility = displayStyle;
    document.getElementById('infoMessage').style.visibility = displayStyle;
    document.getElementById('testsTable').style.visibility = displayStyle;
}

function selectValueCombo(selectObj) {
    var selectIndex = selectObj.selectedIndex;
    var selectValue = selectObj.options[selectIndex].text;
    if (selectValue === "Of any of the build's tests") {
        selectCleanupTest("hidden");
    } else {
        selectCleanupTest("visible");
    }
}

function copyPasteRerunSettings(){
    var checkedTests = document.getElementsByName("rerunSettingsModels.checked");
    var rerunsList = document.getElementsByName('rerunSettingsModels.numberOfReruns');
    var cleanupTestList = document.getElementsByName('rerunSettingsModels.cleanupTest');
    var index = 0;

    var cleanupTest = document.getElementsByName("uftSettingsModel.cleanupTest")[0].value;
    var numberOfReruns = document.getElementsByName("numberOfReruns")[0].value;

    rerunsList.forEach(function(element){
        if (checkedTests[index].checked) {
            element.value = numberOfReruns;
        }
        index = index + 1;
    });

    index = 0;
    cleanupTestList.forEach(function(element){
        if (checkedTests[index].checked) {
            element.value = cleanupTest;
        }
        index = index + 1;
    });
}


function addCleanupTest(cleanupTest) {
    var selectCleanupLists = document.getElementsByName("rerunSettingsModels.cleanupTests");

    selectCleanupLists.forEach(function(element){
        var option = document.createElement("option");
        option.text = cleanupTest;
        option.value = cleanupTest;
        element.add(option);
    });
}

function clearRerunSettings(){
    var checkBoxes = document.getElementsByName("rerunSettingsModels.checked");
    checkBoxes.forEach(function(element){
       element.checked = false;
    });

    var numberOfRerunsFields = document.getElementsByName("rerunSettingsModels.numberOfReruns");
    numberOfRerunsFields.forEach(function(element){
        element.value = 0;
    });

    var selectCleanupLists = document.getElementsByName("rerunSettingsModels.cleanupTest");
    selectCleanupLists.forEach(function(element){
        element.value = "";
    });
}
