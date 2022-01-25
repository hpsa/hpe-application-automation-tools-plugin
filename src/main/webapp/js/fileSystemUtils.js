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

    if (checkbox1.checked) {
        document.getElementsByName("fsTestType")[0].disabled = false;
        document.getElementsByName("selectedNode")[0].disabled = false;

    } else {
        document.getElementsByName("fsTestType")[0].disabled = true;
        document.getElementsByName("selectedNode")[0].disabled = true;
    }

    var selectIndex = document.getElementsByName("fsTestType")[0].selectedIndex;
    var selectValue = document.getElementsByName("fsTestType")[0].options[selectIndex].text;
    if (selectValue === "Rerun the entire set of tests" || selectValue === "Rerun only failed tests") {
        selectCleanupTest("none");
    } else {
        selectCleanupTest("block");
    }
}, false);

    function useAuthentication(obj) {
        if (obj.checked) {
            document.getElementsByName("runfromfs.fsProxyUserName")[0].disabled = null;
            document.getElementsByName("runfromfs.fsProxyPassword")[0].disabled = null;
        } else {
            document.getElementsByName("runfromfs.fsProxyUserName")[0].disabled = "true";
            document.getElementsByName("runfromfs.fsProxyPassword")[0].disabled = "true";
        }
    }

    function enableCombobox(object) {
        if (object.checked) {
            document.getElementsByName("fsTestType")[0].disabled = false;
            document.getElementsByName("selectedNode")[0].disabled = false;
            //document.getElementById("checkBox2").disabled=false;
        } else {
            document.getElementsByName("fsTestType")[0].disabled = true;
            document.getElementsByName("selectedNode")[0].disabled = true;
            //document.getElementById("checkBox2").disabled=true;
        }
    }

    function fileSelected(input) {
        var selectIndex = document.getElementById('testTypeSelect').selectedIndex;
        var selectValue = document.getElementById('testTypeSelect').options[selectIndex].text;
        if (selectValue === "Rerun the entire set of tests" || selectValue === "Rerun only failed tests") {
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
                //document.getElementById("checkBox2").disabled = false;
            } else {
                selectCleanupTest("visible");
                // document.getElementById("checkBox2").disabled = true;
            }
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

    function checkIfPipelineAndUpdateHelpMsg(msg) {
        setTimeout(function () {
            if (window.location.href.indexOf("pipeline-syntax") > 0) {
                let helpText = document.getElementById("helpTextMsg");

                // verify if the element is found, otherwise an exception will occur which blocks the page loading
                if (helpText) {
                    helpText.innerHTML = msg;
                }
            }
        }, 200);
    }

    // holder, which contain all the valid parameter input types
    let selectableTypeList = '';

    function startListenersForParameterInputs() {
        const inputs = document.getElementsByName("fsTestParameter");
        if (inputs) {
            inputs.forEach(elem => elem.addEventListener("change", generateAndPutJSONResult));
        } else {
            console.warn("Test parameter input fields are missing.");
        }

        const delBtns = document.getElementsByName("fsDelParameter");
        if (delBtns) {
            delBtns.forEach(elem => elem.addEventListener("click", deleteParameter));
        } else {
            console.warn("Delete buttons for input fields are missing.");
        }

        let testInput = document.getElementsByName("runfromfs.fsTests")[0];
        if (testInput) {
            const rowInputs = document.querySelectorAll(".testParameter > div > input[type='number']");
            rowInputs.forEach(rowInput => rowInput.setAttribute("max", testInput.value.split("\n").filter(row => row !== "").length.toString()));

            testInput.addEventListener("change", () => {
                const rowInputs = document.querySelectorAll(".testParameter > div > input[type='number']");
                rowInputs.forEach(rowInput => rowInput.setAttribute("max", testInput.value.split("\n").filter(row => row !== "").length.toString()));
            });
        } else {
            console.warn("Test input text area is missing.");
        }
    }

    function startListenerForParameterBlock() {
        let specifyParametersCheckbox = document.getElementsByName("areParametersEnabled");

        if (specifyParametersCheckbox) {
            specifyParametersCheckbox = specifyParametersCheckbox[0];
            specifyParametersCheckbox.addEventListener("click", cleanParameterInput);
        }
    }

    function generateAndPutJSONResult() {
        const inputs = document.getElementsByName("fsTestParameter");
        let inputJSON = [];
        const parameterResultStr = document.getElementsByName("fsParameterJson")[0];

        if (parameterResultStr.length === 0) return console.warn("Parameter input JSON result hidden field is missing, reload the page.");

        inputs.forEach(elem => {
            let curr = {};
            const testIdx = curr["index"] = elem.querySelector(`#parameterInputRow${elem.dataset.index}`).value;
            const name = curr["name"] = elem.querySelector(`#parameterInputName${elem.dataset.index}`).value;

            if (name !== "") {
                curr["type"] = elem.querySelector(`#parameterInputType${elem.dataset.index}`).value;

                const val = elem.querySelector(`#parameterInputValue${elem.dataset.index}`);
                if (curr["type"] === "Boolean") {
                    curr["value"] = val.checked;
                } else if (curr["type"] === "Date") {
                    const date = new Date(val.value);
                    curr["value"] = `${date.getDate() < 10 ? '0' + date.getDate() : date.getDate()}/${date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth()}/${date.getFullYear()}`;
                } else {
                    curr["value"] = val.value;
                }

                inputJSON.push(curr);
            }
        });

        console.log(inputJSON);
        parameterResultStr.value = JSON.stringify(inputJSON);
    }

    function cleanParameterInput() {
        if (this.checked) {
            loadParameterInputs();
        } else {
            const parameterResultStr = document.getElementsByName("fsParameterJson")[0];
            parameterResultStr.value = JSON.stringify([]);
        }
    }

    function addNewParameter() {
        const parametersContainer = document.querySelector("ul[name='fsTestParameters']");
        const parameters = document.getElementsByName("fsTestParameter") || [];
        const nextIdx = parameters.length !== 0 ? parseInt(Array.from(parameters).reduce((prev, curr) => {
            if (parseInt(prev.dataset.index) > parseInt(curr.dataset.index)) return prev;

            return curr;
        }).dataset.index) + 1 : 1;

        const elem = `
        <li class="testParameter" name="fsTestParameter" data-index="${nextIdx}">
            <div>
                <input class="setting-input" name="fsParameterInput" id="parameterInputRow${nextIdx}" min="1" type="number" required="required" />
            </div>
            <div>
                <input class="setting-input" name="fsParameterInput" id="parameterInputName${nextIdx}" type="text" required="required" />
            </div>
            <div>
                <input class="setting-input" name="fsParameterInput" id="parameterInputValue${nextIdx}" type="text"/>
            </div>
            <div>
                <select name="fsParameterInput" id="parameterInputType${nextIdx}">
                    ${selectableTypeList}
                </select>
            </div>  
            <span class="yui-button danger" id="delParameterInput${nextIdx}" name="fsDelParameter">
                <span class="first-child">
                    <button type="button" tabindex="0" >&#9747;</button>
                </span>
            </span>
        </li>
        `;

        parametersContainer.insertAdjacentHTML("beforeend", elem);

        const typeField = document.querySelector(`#parameterInputType${nextIdx}`);
        const valueField = document.querySelector(`#parameterInputValue${nextIdx}`);

        typeField.addEventListener("change", () => {
            valueField.value = "";
            valueField.setAttribute("type", mapForTypeAssociations[typeField.value] || "text");
        });

        startListenersForParameterInputs();
    }

    function deleteParameter(e) {
        this.parentNode.remove();
        startListenersForParameterInputs();
        generateAndPutJSONResult();
    }

    const mapForTypeAssociations = {
        String: 'text',
        Number: 'number',
        Boolean: 'checkbox',
        Password: 'password',
        Date: 'date',
        Any: 'text'
    };

    function loadParameterInputs() {
        const parameterResultStr = document.getElementsByName("fsParameterJson")[0];

        if (parameterResultStr.value === "") return;

        const json = JSON.parse(parameterResultStr.value);

        for (let i = 0; i < json.length; ++i) addNewParameter();

        const parameters = document.getElementsByName("fsTestParameter");

        for (let i = 0; i < json.length; ++i) {
            const currElem = parameters[i];
            const currElemVal = json[i];

            currElem.querySelector(`#parameterInputRow${currElem.dataset.index}`).value = currElemVal["index"] || 1;
            currElem.querySelector(`#parameterInputName${currElem.dataset.index}`).value = currElemVal["name"] || "";
            const valueField = currElem.querySelector(`#parameterInputValue${currElem.dataset.index}`)
            const typeField = currElem.querySelector(`#parameterInputType${currElem.dataset.index}`);
            typeField.value = currElemVal["type"] || "String";

            valueField.setAttribute("type", mapForTypeAssociations[typeField.value] || "text");
            if (typeField.value === "Boolean") {
                valueField.checked = currElemVal["value"] || false;
            } else if (typeField.value === "Date") {
                const date = new Date(currElemVal["value"].split("/").reverse().join("-")) || Date.now();
                valueField.value = `${date.getFullYear()}-${date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : (date.getMonth() + 1)}-${date.getDate() < 10 ? '0' + date.getDate() : date.getDate()}`;
            } else {
                valueField.value = currElemVal["value"] || "";
            }
        }
    }

    function addToSelectableTypeList(type) {
        selectableTypeList += `<option value="${type}">${type}</option>`;
    }