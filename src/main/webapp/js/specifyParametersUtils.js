/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
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

function setupParamSpecification() {
    let main = null;
    if (document.location.href.indexOf("pipeline-syntax") > 0) {
        main = document;
    } else if (document.currentScript) {
        main = document.currentScript.parentElement.closest(BUILDER_SELECTOR);
    }

    setTimeout(() => {
        startListening4Params(main);
    }, 200);
}

function startListening4Params(mainContainer) {
    let main = mainContainer;
    if (mainContainer == null) {
        let divs = document.querySelectorAll(BUILDER_SELECTOR);
        main = divs[divs.length - 1];
    }

    loadParamInputs(main);

    const btnAddNewParam = main.querySelector("button[name='addNewParamBtn']");
    if (btnAddNewParam) {
        btnAddNewParam.addEventListener('click', () => {
            addNewParam(main);
        });
    } else {
        console.warn("Add parameter button is missing.");
    }

    const updateMaxNumber4Spinner = (testInput) => {
        const rowInputs = main.querySelectorAll(".test-param > div > .num-of-test-spinner");
        const newMax = testInput.value.split("\n").filter(row => row !== "").length;
        rowInputs.forEach(rowInput => rowInput.setAttribute("max", newMax === 0 ? 1 : newMax.toString()));
    }
    const updateTest = (container, spinner, testInput) => {
        const testLabel = spinner.parentElement.nextElementSibling.querySelector(".test-label");
        if (spinner.value === '') {
            testLabel.value = "";
            return;
        }
        testLabel.value = testInput.value.split("\n")[parseInt(spinner.value) - 1] || "Please, specify tests first";
    }

    let testInput;

    const prepareTestInput = () => {
        testInput = queryTestInput(main);
        if (testInput) {
            testInput.addEventListener("change", () => {
                updateMaxNumber4Spinner(testInput);

                rowInputs.forEach((rowInput) => {
                    updateTest(main, rowInput, testInput);
                });
            });
            testInput.dispatchEvent(new Event("change"));
        } else {
            console.warn("Test input text area is missing.");
        }
    }

    const rowInputs = main.querySelectorAll(".test-param > div > .num-of-test-spinner");
    prepareTestInput();
    rowInputs.forEach(rowInput => {
        rowInput.addEventListener("click", () => {
            updateTest(main, rowInput, testInput);
        });
        rowInput.addEventListener("change", () => {
            updateTest(main, rowInput, testInput);
        })
    });

    const chkAreParamsEnabled = main.querySelector("input[name='areParamsEnabled']");
    if (chkAreParamsEnabled) {
        chkAreParamsEnabled.addEventListener("click", () => cleanParamInput(main));
    }

    const expandTestsFieldBtn = main.querySelector(".expanding-input__button input[type='button']");
    expandTestsFieldBtn && expandTestsFieldBtn.addEventListener("click", () => {
        prepareTestInput();
    });
}

function queryTestInput(container) {
    return container.querySelector("textarea[name='runfromfs.fsTests'], input[name='runfromfs.fsTests'], textarea[name='runfromalm.almTestSets'], input[name='runfromalm.almTestSets']");
}

function generateAndPutJSONResult(container) {
    const paramsContainer = container.querySelector("ul[name='testParams']");

    const inputs = paramsContainer.querySelectorAll("li[name='testParam']");
    let inputJSON = [];

    const strParamRes = paramsContainer.parentElement.querySelector("input.json-params");

    if (!strParamRes) return console.warn("Param input JSON result hidden field is missing, reload the page.");

    inputs.forEach(elem => {
        let curr = {};
        const idx = elem.dataset.index;
        curr.index = elem.querySelector(`#paramInputRow_${idx}`).value;
        const name = curr.name = elem.querySelector(`#paramInputName_${idx}`).value;

        if (name !== "") {
            curr.type = elem.querySelector(`#paramInputType_${elem.dataset.index}`).value;

            const val = elem.querySelector(`#paramInputValue_${elem.dataset.index}`);
            if (curr.type === "Boolean") {
                curr.value = val.checked;
            } else if (curr.type === "Date" || curr.type === "DateTime") {
                const date = new Date(val.value);
                curr.value = `${date.getDate() < 10 ? '0' + date.getDate() : date.getDate()}/${date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth()}/${date.getFullYear()}`;
            } else {
                curr.value = val.value;
            }

            inputJSON.push(curr);
        }
    });

    strParamRes.value = normalizeJsonFormat(JSON.stringify(inputJSON));
}

function cleanParamInput(container) {
    if (this.checked) {
        loadParamInputs(container);
    } else {
        const strParamRes = container.querySelector("input.json-params");
        if (!strParamRes) return console.warn("Param input JSON result hidden field is missing, reload the page.");
        strParamRes.value = normalizeJsonFormat(JSON.stringify([]));
    }
}

function addNewParam(container) {
    const paramContainer = container.querySelector("ul[name='testParams']");
    const params = paramContainer.querySelectorAll("li[name='testParam']") || [];
    const nextIdx = params.length !== 0 ? parseInt(Array.from(params).reduce((prev, curr) => {
        if (parseInt(prev.dataset.index) > parseInt(curr.dataset.index)) return prev;

        return curr;
    }).dataset.index) + 1 : 1;

    let maxNumOfTests = 1;
    let testInput = queryTestInput(container);
    if (testInput) {
        maxNumOfTests = testInput.value.split("\n").filter(row => row !== "").length.toString();
    } else {
        console.warn("Test input field is missing.");
    }

    const elem = `
        <li class="test-param" name="testParam" data-index="${nextIdx}">
            <div>
                <input class="jenkins-input setting-input num-of-test-spinner" name="paramInput" id="paramInputRow_${nextIdx}" min="1" max="${maxNumOfTests === 0 ? 1 : maxNumOfTests}" type="number" required="required" />
            </div>
            <div>
                <input class="jenkins-input setting-input test-label" name="paramInput" id="paramInputTest_${nextIdx}" type="text" value="" disabled />
            </div>
            <div>
                <input class="jenkins-input setting-input" name="paramInput" id="paramInputName_${nextIdx}" type="text" required="required" />
            </div>
            <div>
                <input class="jenkins-input setting-input" name="paramInput" id="paramInputValue_${nextIdx}" type="text"/>
            </div>
            <div>
                <select name="paramInput" id="paramInputType_${nextIdx}">
                    ${selectableTypeList}
                </select>
            </div>  
            <span class="yui-button danger" id="delParamInput_${nextIdx}" name="delParam">
                <span class="first-child">
                    <button type="button" tabindex="0">&#9747;</button>
                </span>
            </span>
        </li>
        `;

    paramContainer.insertAdjacentHTML("beforeend", elem);

    const testLabel = paramContainer.querySelector(`#paramInputTest_${nextIdx}`);
    const spinner = paramContainer.querySelector(`#paramInputRow_${nextIdx}`);

    const handleSpinner = () => {
        if (spinner.value === '') {
            testLabel.value = "";
            return;
        }

        testLabel.value = queryTestInput(container).value.split("\n")[parseInt(spinner.value) - 1] || "Please, specify tests first";
    };
    spinner.addEventListener("click", () => {
        handleSpinner();
    });
    spinner.addEventListener("change", () => {
        handleSpinner();
    });

    spinner.dispatchEvent(new Event("change"));

    Array.from(paramContainer.querySelectorAll(`[name='paramInput']`)).filter(input => input.getAttribute("id").endsWith("_" + nextIdx.toString()))
        .forEach(input => input.addEventListener("change", () => generateAndPutJSONResult(container)));

    const delButton = paramContainer.querySelector(`#delParamInput_${nextIdx} > span > button`);
    delButton.addEventListener("click", () => deleteParam(delButton, container));

    const typeField = paramContainer.querySelector(`#paramInputType_${nextIdx}`);
    const valueField = paramContainer.querySelector(`#paramInputValue_${nextIdx}`);
    typeField.addEventListener("change", () => {
        valueField.value = "";
        valueField.setAttribute("type", map4TypeAssociations[typeField.value] || "text");
    });
}

function deleteParam(elem, container) {
    elem.parentNode.parentNode.parentNode.remove();
    generateAndPutJSONResult(container);
}

// has to be declared like this, because it has to be globally accessible and multiple steps can be added in a single job, which would throw duplicate exception
if (typeof map4TypeAssociations === "undefined") {
    map4TypeAssociations = {
        String: 'text',
        Number: 'number',
        Boolean: 'checkbox',
        Password: 'password',
        Date: 'date',
        Any: 'text',
        Float: 'number',
        Double: 'number',
        Decimal: 'number',
        Long: 'number',
        DateTime: 'date',
        Int: 'number'
    };
}

function loadParamInputs(container) {
    const paramResultStr = container.querySelector("input.json-params");

    // on some browsers the value may return with extra-quotes
    let params = paramResultStr.value;

    if (params === "" || params === "[]" || params === "\"[]\"") return;

    let json;
    try {
        json = JSON.parse(normalizeJsonFormat(params));
    } catch (e) {
        json = JSON.parse("[]");
    }

    // has to be an object to be valid JSON input, otherwise because of security policies the JSON was altered
    if (typeof(json) === "string") json = JSON.parse("[]");

    for (let i = 0; i < json.length; ++i) addNewParam(container);

    const testParams = container.querySelectorAll("li[name='testParam']");

    for (let i = 0; i < json.length; ++i) {
        const currElem = testParams[i];
        const currElemVal = json[i];

        currElem.querySelector(`#paramInputRow_${currElem.dataset.index}`).value = currElemVal["index"] || 1;
        currElem.querySelector(`#paramInputName_${currElem.dataset.index}`).value = currElemVal["name"] || "";
        const valueField = currElem.querySelector(`#paramInputValue_${currElem.dataset.index}`)
        const typeField = currElem.querySelector(`#paramInputType_${currElem.dataset.index}`);
        typeField.value = currElemVal["type"] || "String";

        valueField.setAttribute("type", map4TypeAssociations[typeField.value] || "text");
        if (typeField.value === "Boolean") {
            valueField.checked = currElemVal["value"] || false;
        } else if (typeField.value === "Date" || typeField.value === "DateTime") {
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

function addSeparatorToTypeList(group, idx, groupsLength) {
    // do not populate the list again, if already populated
    // happens when multiple jobs with parameters table is added to the base job
    if (selectableTypeList.split("</optgroup>").length - 1 >= groupsLength) return;

    // special command, called with idx of -1 and group of null if separator needed
    if (idx === -1 && group === null) {
        selectableTypeList += '</optgroup>';
        return;
    }

    selectableTypeList += `<optgroup label="${group}">`;
}

function normalizeJsonFormat(str) {
    // because of certain security policies, on some servers the special characters could be escaped twice, we need to parse them twice
    let ret = str;
    if (str.endsWith("\"")) ret = JSON.parse(ret);
    return ret;
}