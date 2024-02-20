if (window.NodeList && !NodeList.prototype.forEach) {
	NodeList.prototype.forEach = Array.prototype.forEach;
}
if (typeof CredScope == "undefined") {
	CredScope = {JOB : "JOB", SYSTEM : "SYSTEM"};
}
if (typeof RUN_FROM_ALM_BUILDER_SELECTOR == "undefined") {
	RUN_FROM_ALM_BUILDER_SELECTOR = 'div[name="builder"][descriptorid="com.microfocus.application.automation.tools.run.RunFromAlmBuilder"]';
}
function setupAlmCredentials() {
	let divMain = null;
	if (document.location.href.indexOf("pipeline-syntax")>0) { // we are on pipeline-syntax page, where runFromAlmBuilder step can be selected only once, so it's ok to use document
		divMain = document;
	} else if (document.currentScript) { // this block is used for non-IE browsers, for the first ALM build step only, it finds very fast the parent DIV (containing all ALM controls)
		divMain = document.currentScript.parentElement.closest(RUN_FROM_ALM_BUILDER_SELECTOR);
	}
	setTimeout( function() {
		prepareTask(divMain)}, 200);
}
function prepareTask(divMain) {
	if (divMain == null) { // this block is needed for IE, but also for non-IE browsers when adding more than one ALM build step
		let divs = document.querySelectorAll(RUN_FROM_ALM_BUILDER_SELECTOR);
		divMain = divs[divs.length-1];
	}

	const lstServerName = divMain.querySelector('select.alm-server-name');
	const lstCredentialsScope = divMain.querySelector('select[name="almCredentialsScope"]');
	const chkSsoEnabled = divMain.querySelector('input[type="checkbox"][name="runfromalm.isSSOEnabled"]');
	const divSysAlmUsername = divMain.querySelector('div.sys-alm-username');
	const divSysAlmClientId = divMain.querySelector('div.sys-alm-client-id');
	const divJobAlmUsername = divMain.querySelector('div.job-alm-username');
	const divJobAlmPwd = divMain.querySelector('div.job-alm-password');
	const divJobAlmClientId = divMain.querySelector('div.job-alm-client-id');
	const divJobAlmSecret = divMain.querySelector('div.job-alm-secret');

	selectCredentialsType();
	if (typeof chkSsoEnabled.onclick !== "function") {
		chkSsoEnabled.onclick = selectCredentialsType;
	}
	if (typeof lstCredentialsScope.onchange !== "function") {
		lstCredentialsScope.onchange = selectCredentialsType;
	}
	if (typeof lstServerName.onchange !== "function") {
		lstServerName.onchange = resetCredentials;
	}
	function selectCredentialsType() {
		const isJobScope = lstCredentialsScope.value === CredScope.JOB;
		const isSSO = chkSsoEnabled.checked;

		if (isJobScope) {
			[divSysAlmUsername, divSysAlmClientId].forEach(function(div) { div.querySelector("select").name += "_x"; div.style.display = "none"; });
			divJobAlmUsername.querySelector("input").name = "almUserName";
			divJobAlmPwd.querySelector("input").name = "almPassword";
			divJobAlmClientId.querySelector("input").name = "almClientID";
			divJobAlmSecret.querySelector("input").name = "almApiKey";
			divJobAlmUsername.style.display = divJobAlmPwd.style.display = isSSO ? "none" : "block";
			divJobAlmClientId.style.display = divJobAlmSecret.style.display = isSSO ? "block" : "none";
		} else {
			divSysAlmUsername.querySelector("select").name = "almUserName";
			divSysAlmClientId.querySelector("select").name = "almClientID";
			[divJobAlmUsername, divJobAlmPwd, divJobAlmClientId, divJobAlmSecret].forEach(function(div) { div.querySelector("input").name += "_x"; div.style.display = "none"; });
			divSysAlmUsername.style.display = isSSO ? "none" : "block";
			divSysAlmClientId.style.display = isSSO ? "block" : "none";
		}
	}
	function resetCredentials() {
		let evt;
		try {
			evt = new UIEvent("change", { "view": window, "bubbles": false, "cancelable": true });
		} catch(e) { // IE does not support UIEvent constructor
			evt = document.createEvent("UIEvent");
			evt.initUIEvent("change", false, true, window, 1);
		}
		[divJobAlmSecret, divJobAlmPwd].forEach(function(div) {
			const btnUpdate = div.querySelector(".hidden-password-update-btn");
			btnUpdate && btnUpdate.click();
		});
		[divJobAlmClientId, divJobAlmUsername, divJobAlmSecret, divJobAlmPwd].forEach(function(div) {
			const input = div.querySelector("input");
			input.value = "";
			input.dispatchEvent(evt);
		});
	}
}
