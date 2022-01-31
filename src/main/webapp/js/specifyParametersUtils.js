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

// holder, which contain all the valid parameter input types
let selectableTypeList = '';

function startListenersForParameterInputs() {
    const inputs = document.getElementsByName("testParameter");
    if (inputs) {
        inputs.forEach(elem => elem.addEventListener("change", generateAndPutJSONResult));
    } else {
        console.warn("Test parameter input fields are missing.");
    }

    const delBtns = document.getElementsByName("delParameter");
    if (delBtns) {
        delBtns.forEach(elem => elem.addEventListener("click", deleteParameter));
    } else {
        console.warn("Delete buttons for input fields are missing.");
    }

    let testInput = document.getElementsByName("runfromfs.fsTests")[0] || document.getElementsByName("runfromalm.almTestSets")[0];
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
    const inputs = document.getElementsByName("testParameter");
    let inputJSON = [];
    const parameterResultStr = document.getElementsByName("parameterJson")[0];

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

    parameterResultStr.value = JSON.stringify(inputJSON);
}

function cleanParameterInput() {
    if (this.checked) {
        loadParameterInputs();
    } else {
        const parameterResultStr = document.getElementsByName("parameterJson")[0];
        parameterResultStr.value = JSON.stringify([]);
    }
}

function addNewParameter() {
    const parametersContainer = document.querySelector("ul[name='testParameters']");
    const parameters = document.getElementsByName("testParameter") || [];
    const nextIdx = parameters.length !== 0 ? parseInt(Array.from(parameters).reduce((prev, curr) => {
        if (parseInt(prev.dataset.index) > parseInt(curr.dataset.index)) return prev;

        return curr;
    }).dataset.index) + 1 : 1;

    const elem = `
        <li class="testParameter" name="testParameter" data-index="${nextIdx}">
            <div>
                <input class="setting-input" name="parameterInput" id="parameterInputRow${nextIdx}" min="1" type="number" required="required" />
            </div>
            <div>
                <input class="setting-input" name="parameterInput" id="parameterInputName${nextIdx}" type="text" required="required" />
            </div>
            <div>
                <input class="setting-input" name="parameterInput" id="parameterInputValue${nextIdx}" type="text"/>
            </div>
            <div>
                <select name="parameterInput" id="parameterInputType${nextIdx}">
                    ${selectableTypeList}
                </select>
            </div>  
            <span class="yui-button danger" id="delParameterInput${nextIdx}" name="delParameter">
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
    const parameterResultStr = document.getElementsByName("parameterJson")[0];

    if (parameterResultStr.value === "") return;

    const json = JSON.parse(parameterResultStr.value);

    for (let i = 0; i < json.length; ++i) addNewParameter();

    const parameters = document.getElementsByName("testParameter");

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