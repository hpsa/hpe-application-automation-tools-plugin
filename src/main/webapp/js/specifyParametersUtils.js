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

// has to be declared like this, because it has to be globally accessible and multiple steps can be added in a single job, which would throw duplicate exception
// holder, which contain all the valid parameter input types
if (typeof selectableTypeList === "undefined") {
    selectableTypeList = '';
}

if (typeof BUILDER_SELECTOR === "undefined") {
    BUILDER_SELECTOR = "div[name='builder'][descriptorid*='com.microfocus.application.automation.tools.run.RunFrom']";
}

function setupParameterSpecification() {
    let main = null;
    if (document.location.href.indexOf("pipeline-syntax") > 0) {
        main = document;
    } else if (document.currentScript) {
        main = document.currentScript.parentElement.closest(BUILDER_SELECTOR);
    }

    setTimeout(() => {
        startListeningForParameters(main);
    }, 200);
}

function startListeningForParameters(mainContainer) {
    let main = mainContainer;
    if (mainContainer == null) {
        let divs = document.querySelectorAll(BUILDER_SELECTOR);
        main = divs[divs.length - 1];
    }

    loadParamInputs(main);

    const btnAddNewParam = main.querySelector("button[name='addNewParameterBtn']");
    if (btnAddNewParam) {
        btnAddNewParam.addEventListener('click', () => {
            addNewParam(main);
        });
    } else {
        console.warn("Add parameter button is missing.");
    }

    const updateMaxNumberForSpinner = () => {
        const rowInputs = main.querySelectorAll(".testParameter > div > .numOfTestSpinner");
        rowInputs.forEach(rowInput => rowInput.setAttribute("max", testInput.value.split("\n").filter(row => row !== "").length.toString()));
    }
    const queryTestInput = () => main.querySelector("textarea[name='runfromfs.fsTests'], input[name='runfromfs.fsTests']") || main.querySelector("textarea[name='runfromalm.almTestSets'], input[name='runfromalm.almTestSets']");

    let testInput = queryTestInput();
    if (testInput) {
        updateMaxNumberForSpinner();
        testInput.addEventListener("change", updateMaxNumberForSpinner);
    } else {
        console.warn("Test input text area is missing.");
    }

    const chkAreParamsEnabled = main.querySelector("input[name='areParametersEnabled']");
    if (chkAreParamsEnabled) {
        chkAreParamsEnabled.addEventListener("click", () => cleanParamInput(main));
    }

    const expandTestsFieldBtn = main.querySelector(".expanding-input__button input[type='button']");
    expandTestsFieldBtn && expandTestsFieldBtn.addEventListener("click", () => {
        testInput = queryTestInput();

        if (testInput) {
            testInput.addEventListener("change", updateMaxNumberForSpinner);
        } else {
            console.warn("Test input text area is missing.");
        }
    })
}

function generateAndPutJSONResult(container) {
    const paramsContainer = container.querySelector("ul[name='testParameters']");

    const inputs = paramsContainer.querySelectorAll("li[name='testParameter']");
    let inputJSON = [];

    const strParamRes = paramsContainer.parentElement.querySelector("input[name='parameterJson']");

    if (!strParamRes) return console.warn("Parameter input JSON result hidden field is missing, reload the page.");

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

    strParamRes.value = JSON.stringify(inputJSON);
}

function cleanParamInput(container) {
    if (this.checked) {
        loadParamInputs(container);
    } else {
        const strParamRes = container.querySelector("input[name='parameterJson']");

        if (!strParamRes) return console.warn("Parameter input JSON result hidden field is missing, reload the page.");

        strParamRes.value = JSON.stringify([]);
    }
}

function addNewParam(container) {
    const paramContainer = container.querySelector("ul[name='testParameters']");
    const params = paramContainer.querySelectorAll("li[name='testParameter']") || [];
    const nextIdx = params.length !== 0 ? parseInt(Array.from(params).reduce((prev, curr) => {
        if (parseInt(prev.dataset.index) > parseInt(curr.dataset.index)) return prev;

        return curr;
    }).dataset.index) + 1 : 1;

    let maxNumOfTests = 1;
    const testInput = container.querySelector("textarea[name='runfromfs.fsTests'], input[name='runfromfs.fsTests']") || container.querySelector("textarea[name='runfromalm.almTestSets'], input[name='runfromalm.almTestSets']");
    if (testInput) {
        maxNumOfTests = testInput.value.split("\n").filter(row => row !== "").length.toString();
    } else {
        console.warn("Test input field is missing.");
    }

    const elem = `
        <li class="testParameter" name="testParameter" data-index="${nextIdx}">
            <div>
                <input class="setting-input numOfTestSpinner" name="parameterInput" id="parameterInputRow${nextIdx}" min="1" max="${maxNumOfTests}" type="number" required="required" />
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
                    <button type="button" tabindex="0">&#9747;</button>
                </span>
            </span>
        </li>
        `;

    paramContainer.insertAdjacentHTML("beforeend", elem);

    Array.from(paramContainer.querySelectorAll(`[name='parameterInput']`)).filter(input => input.getAttribute("id").includes(nextIdx.toString()))
        .forEach(input => input.addEventListener("change", () => generateAndPutJSONResult(container)));

    const delButton = paramContainer.querySelector(`#delParameterInput${nextIdx} > span > button`);
    delButton.addEventListener("click", () => deleteParam(delButton, container));

    const typeField = paramContainer.querySelector(`#parameterInputType${nextIdx}`);
    const valueField = paramContainer.querySelector(`#parameterInputValue${nextIdx}`);
    typeField.addEventListener("change", () => {
        valueField.value = "";
        valueField.setAttribute("type", mapForTypeAssociations[typeField.value] || "text");
    });
}

function deleteParam(elem, container) {
    elem.parentNode.parentNode.parentNode.remove();
    generateAndPutJSONResult(container);
}

// has to be declared like this, because it has to be globally accessible and multiple steps can be added in a single job, which would throw duplicate exception
if (typeof mapForTypeAssociations === "undefined") {
    mapForTypeAssociations = {
        String: 'text',
        Number: 'number',
        Boolean: 'checkbox',
        Password: 'password',
        Date: 'date',
        Any: 'text'
    };
}

function loadParamInputs(container) {
    const parameterResultStr = container.querySelector("input[name='parameterJson']");

    if (parameterResultStr.value === "") return;

    const json = JSON.parse(parameterResultStr.value);

    for (let i = 0; i < json.length; ++i) addNewParam(container);

    const parameters = container.querySelectorAll("li[name='testParameter']");

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

function addToSelectableTypeList(type, typeListLength) {
    // if there are more build steps than one, do not populate the dropdown
    // if the dropdown is already populated, do not populate it again
    if (selectableTypeList.split("</option>").length - 1 >= typeListLength) return;

    selectableTypeList += `<option value="${type}">${type}</option>`;
}