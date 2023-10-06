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
if (typeof RUN_FROM_FS_BUILDER_SELECTOR == "undefined") {
	RUN_FROM_FS_BUILDER_SELECTOR = 'div[name="builder"][descriptorid="com.microfocus.application.automation.tools.run.RunFromFileBuilder"]';
}
const templates = { "regions": [ "Europe (Frankfurt)" ], "os": [ "Windows Server 2022" ], "browsers": [ { "type": "Chrome", "versions": [ { "version": "117", "tag": "117" }, { "version": "117", "tag": "latest" }, { "version": "116", "tag": "latest-1" }, { "version": "116", "tag": "116" }, { "version": "115", "tag": "latest-2" }, { "version": "115", "tag": "115" } ] }, { "type": "Edge", "versions": [ { "version": "117", "tag": "117" }, { "version": "117", "tag": "latest" }, { "version": "116", "tag": "latest-1" }, { "version": "116", "tag": "116" }, { "version": "115", "tag": "latest-2" }, { "version": "115", "tag": "115" } ] }, { "type": "Firefox", "versions": [ { "version": "117", "tag": "117" }, { "version": "117", "tag": "latest" }, { "version": "116", "tag": "latest-1" }, { "version": "116", "tag": "116" }, { "version": "115", "tag": "latest-2" }, { "version": "115", "tag": "115" } ] } ] }

function getDigitalLab(divMain) {
    const dl = divMain.querySelector("#mobileSpecificSection");
    const o = { serverName: dl.querySelector('select[name="mcServerName"]').value,
        userName: "",
        password: "",
        tenantId: "",
        accessKey: "",
        authType: dl.querySelector('input[name$="authModel"]:checked').value,
        useProxy: dl.querySelector('input[name="mcUseProxy"]').checked,
        proxyAddress: "",
        useProxyAuth: false,
        proxyUserName: "",
        proxyPassword: "",
        err: dl.querySelector("#errorMessage"),
        recreateJob: dl.querySelector('input[name="recreateJob"]').checked,
        jobId: dl.querySelector('input[name="fsJobId"]').value,
        uftOneVersion: dl.querySelector('input[name="uftOneVersion"]').value, // TODO find a better approach
        deviceInfo: dl.querySelector("#deviceInfo")
    };
    if (o.authType == "base") {
        o.userName = dl.querySelector('input[name="mcUserName"]').value;
        o.password = dl.querySelector('input[name="mcPassword"]').value;
        o.tenantId = dl.querySelector('input[name="mcTenantId"]').value;
    } else {
        o.accessKey = dl.querySelector('input[name="mcAccessKey"]').value;
    }
    if (o.useProxy) {
        o.proxyAddress = dl.querySelector('input[name="fsProxyAddress"]').value;
        o.useProxyAuth = dl.querySelector('input[name="mcUseProxyAuth"]').checked;
        if (o.useProxyAuth) {
            o.proxyUserName = dl.querySelector('input[name="fsProxyUserName"]').value;
            o.proxyPassword = dl.querySelector('input[name="fsProxyPassword"]').value;
        }
    }

    return o;
}

function setupDigitalLab() {
	let divMain = null;
	if (document.location.href.indexOf("pipeline-syntax")>0) { // we are on pipeline-syntax page, where runFromFileBuilder step can be selected only once
		divMain = document;
	} else if (document.currentScript) { // this block is used for non-IE browsers, for the first FS build step only, it finds very fast the parent DIV
		divMain = document.currentScript.parentElement.closest(RUN_FROM_FS_BUILDER_SELECTOR);
	}
	setTimeout(function() { prepareDigitalLab(divMain)}, 100);
}
function prepareDigitalLab(divMain) {
	if (divMain == null) { // this block is needed for IE, but also for non-IE browsers when adding more than one FS build step
		let divs = document.querySelectorAll(RUN_FROM_FS_BUILDER_SELECTOR);
		divMain = divs[divs.length - 1];
	}
    const dl = divMain.querySelector("#mobileSpecificSection");
    //TODO show/hide dynamically
    dl.querySelector("#deviceInfo").style.display = "none";
}
function loadInfo(a, b) {
    b.disabled = true;
    try {
        const divMain = b.parentElement.closest(RUN_FROM_FS_BUILDER_SELECTOR);
        const dl = getDigitalLab(divMain);
        const isMcCredentialMissing = ("base" == dl.authType ? (dl.userName.trim() == "" || dl.password.trim() == "") : dl.accessKey.trim() == "");

        const isProxyAddressRequiredButMissing = dl.useProxy && dl.proxyAddress.trim() == "";
        const isProxyCredentialRequiredButMissing = dl.useProxyAuth && (dl.proxyUserName.trim() == "" || dl.proxyPassword.trim() == "");
        if (isMcCredentialMissing || isProxyAddressRequiredButMissing || isProxyCredentialRequiredButMissing) {
            dl.err.style.display = "block";
            b.disabled = false;
            return;
        }

        //TODO use a dynamic flag
        if (b.id == "browserLab") {
            loadBrowserLabInfo(a, b, dl);
        } else if (b.value = "wizard") {
            loadMobileInfo(a, b, dl);
        }
    } catch (e) {
        console.error(e);
    } finally {
        b.disabled = false;
    }
 }

function loadBrowserLabInfo(a, b, o) {
    a.getBrowserLab(o.serverName, o.accessKey, o.useProxyAuth, o.proxyAddress, o.proxyUserName, o.proxyPassword, o.uftOneVersion, function (response) {
        if (response?.responseJSON) {
            const json = response.responseJSON;
            console.log(json);
            o.err.style.display = "none";
        } else {
            o.err.style.display = "block";
        }
        b.disabled = false;
    });
}

function loadMobileInfo(a, b, o) {
    let baseUrl = "";

    a.getMcServerUrl(o.serverName, function (r) {
        baseUrl = r.responseObject();
        if (baseUrl) {
            baseUrl = baseUrl.trim().replace(/[\/]+$/, "");
        } else {
            ParallelRunnerEnv.setEnvironmentError(b, true);//TODO check if this is OK
            b.disabled = false;
            return;
        }
        let prevJobId = o.recreateJob ? "" : o.jobId;
        a.getJobId(baseUrl, o.userName, o.password, o.tenantId, o.accessKey, o.authType, o.useProxyAuth, o.proxyAddress, o.proxyUserName, o.proxyPassword, prevJobId, function (response) {
            var jobId = response.responseObject();
            if (jobId == null) {
                o.err.style.display = "block";
                b.disabled = false;
                return;
            }
            //hide the error message after success login
            o.err.style.display = "none";
            var openedWindow = window.open('/', 'test parameters', 'height=820,width=1130');
            openedWindow.location.href = 'about:blank';
            openedWindow.location.href = baseUrl + "/integration/#/login?jobId=" + jobId + "&displayUFTMode=true";
            var messageCallBack = function (event) {
                if (event && event.data && event.data == "mcCloseWizard") {
                    a.populateAppAndDevice(baseUrl, o.userName, o.password, o.tenantId, o.accessKey, o.authType, o.useProxyAuth, o.proxyAddress, o.proxyUserName, o.proxyPassword, jobId, function (app) {
                        var jobInfo = app.responseObject();
                        let deviceId = "", OS = "", manufacturerAndModel = "", targetLab = "";
                        if (jobInfo['deviceJSON']) {
                            if (jobInfo['deviceJSON']['deviceId']) {
                                deviceId = jobInfo['deviceJSON']['deviceId'];
                            }
                            if (jobInfo['deviceJSON']['OS']) {
                                OS = jobInfo['deviceJSON']['OS'];
                            }
                            if (jobInfo['deviceJSON']['manufacturerAndModel']) {
                                manufacturerAndModel = jobInfo['deviceJSON']['manufacturerAndModel'];
                            }
                        }
                        if (jobInfo['deviceCapability']) {
                            if (jobInfo['deviceCapability']['OS']) {
                                OS = jobInfo['deviceCapability']['OS'];
                            }
                            if (jobInfo['deviceCapability']['manufacturerAndModel']) {
                                manufacturerAndModel = jobInfo['deviceCapability']['manufacturerAndModel'];
                            }
                            if (jobInfo['deviceCapability']['targetLab']) {
                                targetLab = jobInfo['deviceCapability']['targetLab'];
                            }
                        }
                        const div = o.deviceInfo;
                        div.querySelector('input[name="fsDeviceId"]').value = deviceId;
                        div.querySelector('input[name="fsOs"]').value = OS;
                        div.querySelector('input[name="fsManufacturerAndModel"]').value = manufacturerAndModel;
                        div.querySelector('input[name="fsTargetLab"]').value = targetLab;
                        div.querySelector('input[name="fsLaunchAppName"]').value = jobInfo['definitions']['launchApplicationName'];
                        div.querySelector('input[name="fsInstrumented"]').value = jobInfo['definitions']['instrumented'];
                        div.querySelector('input[name="fsAutActions"]').value = jobInfo['definitions']['autActions'];
                        div.querySelector('input[name="fsDevicesMetrics"]').value = jobInfo['definitions']['deviceMetrics'];
                        div.querySelector('input[name="fsExtraApps"]').value = jobInfo['extraApps'];
                        div.querySelector('input[name="fsJobId"]').value = jobInfo['jobUUID'];
                        b.disabled = false;
                        o.err.style.display = "none";
                        window.removeEventListener("message", messageCallBack, false);
                        openedWindow.close();
                    });
                }
            };
            window.addEventListener("message", messageCallBack, false);

            function checkChild() {
                if (openedWindow && openedWindow.closed) {
                    clearInterval(timer);
                    b.disabled = false;
                }
            }

            var timer = setInterval(checkChild, 500);
        });
    });
}

function hideAndMoveAdvancedBody(_id) {
    const tBody = document.querySelector("#" + _id).parentNode; // initial advanced block content
    const initialAdvancedBlock = tBody.previousSibling; // advanced link button block and here was hidden the initial advanced block content
    initialAdvancedBlock.querySelector(".advancedBody").appendChild(tBody); // moves the initial advanced block content back to the hidden block
    initialAdvancedBlock.querySelector(".advancedLink").style.display = ""; // enables once again the advanced link
}

function onCloudBrowserLabClick(a, b, path) {
    const div = b.parentElement.closest("#mobileSpecificSection");
    let dlg = div.querySelector("#cloudBrowsersModal");
    if (dlg == null) {
        dlg = generateModalDialog(path);
        div.appendChild(dlg);
    }

    fillOsesDDL(dlg);
    fillBrowsersAndVersionsDDLs(dlg);
    fillRegionsDDL(dlg);

    dlg.style.display = "block";
    return true;
}

function fillRegionsDDL(dlg) {
    const ddl = dlg.querySelector('select[name="cldBrowserLoc"]');
    const arr = templates.regions.sort();
    fillDDL(ddl, arr);
}

function fillOsesDDL(dlg) {
    const ddl = dlg.querySelector('select[name="cldBrowserOS"]');
    const arr = templates.os;
    fillDDL(ddl, arr);
}

function fillBrowsersAndVersionsDDLs(dlg) {
    const ddlB = dlg.querySelector('select[name="cldBrowser"]');
    const ddlV = dlg.querySelector('select[name="cldBrowserVer"]');
    let mapBV = new Map();
    //const arrB = templates.browsers.map(b => b.type).sort();
    //fillDDL(ddlB, arrB);

    templates.browsers.forEach((obj) => {
        mapBV.set(obj.type, obj.versions?.map(v => v.tag).sort());
    });
    fillBrowsersDDL(ddlB, mapBV);

    ddlB.onchange = (e) => {
        //const arr = getBrowserVersions(e.target.value);
        const arr = ddlB.selectedOptions[0]?.versions;
        if (arr?.length) {
            fillDDL(ddlV, arr);
        } else {
            ddlV.length = 0;
        }
    };

    const arr = ddlB.selectedOptions[0]?.versions;
    arr?.length && fillDDL(ddlV, arr);
    //fillDDL(ddlV, getBrowserVersions(ddlB.value));
}

function getBrowserVersions(type) {
    return templates.browsers.find(b => b.type == type)?.versions.map(b => b.tag).sort();
}

function fillBrowsersDDL(ddl, map) {
    ddl.length = 0;
    if (map?.size) {
        let x = 0;
        map.forEach((val, key) => {
            let opt = new Option(key, key, 0 == x++);
            //opt.dataset.versions = JSON.stringify(val);
            opt.versions = val;
            ddl[ddl.length] = opt;
        });
    }
}

function fillDDL(ddl, arr) {
    ddl.length = 0;
    if (arr?.length) {
        for (var i = 0; i < arr.length; ++i) {
            ddl[ddl.length] = new Option(arr[i], arr[i], i == 0);
        }
    }
}

function hideCloudBrowserModal(b) {
    b.parentElement.closest("div.modal").style.display = "none";
}

function onSaveCloudBrowser(b) {
    //TODO save details
    hideCloudBrowserModal(b);
}

function loadCssIfNotAlreadyLoaded(path) {
    const ss = document.styleSheets;
    for (let i = 0, max = ss.length; i < max; i++) {
        if (ss[i].href == path)
            return;
    }
    let link = document.createElement("link");
    link.rel = "stylesheet";
	link.type = "text/css";
    link.href = path;

    document.head.appendChild(link);
}

function generateModalDialog(rootUrl) {
    loadCssIfNotAlreadyLoaded(rootUrl + "plugin/hp-application-automation-tools-plugin/css/modal_dialog.css");

	var browsersModal = document.createElement("div");
	browsersModal.setAttribute("id","cloudBrowsersModal");
	browsersModal.setAttribute("class","modal");

	let content = document.createElement("div");
	content.setAttribute("class","modal-content");

	let hdr = document.createElement("div");
	hdr.setAttribute("class","modal-header");

	var xDiv = document.createElement("div");
	xDiv.innerHTML = "x";
	xDiv.setAttribute("class","close");
	xDiv.setAttribute("onclick","hideCloudBrowserModal(this)");

	var title = document.createElement("div");
	title.innerHTML = "Choose a browser";
	title.setAttribute("class","modal-title");

	var body = document.createElement("div");
	body.setAttribute("class","modal-body");

    addEmptySelect(body, "Operating System", "cldBrowserOS");
    addEmptySelect(body, "Browser Type", "cldBrowser");
    addEmptySelect(body, "Browser Version", "cldBrowserVer");
    addEmptySelect(body, "Location", "cldBrowserLoc");

	var sDiv = document.createElement('div');
	sDiv.innerHTML = "SAVE";
	sDiv.setAttribute("class","save-text");
	sDiv.setAttribute("onclick",'onSaveCloudBrowser(this)');

	var footer = document.createElement("div");
	footer.setAttribute("class","modal-footer");

	footer.appendChild(sDiv);
	hdr.appendChild(xDiv);
	hdr.appendChild(title);
	content.appendChild(hdr);
	content.appendChild(body);
	content.appendChild(footer);
	browsersModal.appendChild(content);

	return browsersModal;
};

function addEmptySelect(parent, label, name) {
    let div = document.createElement("div");
    div.setAttribute("class", "modal-body-item");
    div.innerHTML = `<label><span>${label}</span><select name="${name}"></select></label>`;
	parent.appendChild(div);
}