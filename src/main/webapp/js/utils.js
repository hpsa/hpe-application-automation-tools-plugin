document.addEventListener('DOMContentLoaded', function() {
    var checkbox = document.getElementById('checkBox1');
    if(checkbox.checked){
        document.getElementsByName("fsTestType")[0].disabled = false;
    }else{
        document.getElementsByName("fsTestType")[0].disabled = true;
    }

    var selectIndex = document.getElementById('testTypeSelect').selectedIndex;
    var selectValue = document.getElementById('testTypeSelect').options[selectIndex].text;
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
        input = document.getElementById('attachment');
        fileSelected(input);
    } else {
        document.getElementsByName("fsTestType")[0].disabled = true;
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
    var numberOfReruns = document.getElementsByName("uftSettingsModel.numberOfReruns")[0].value;

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


