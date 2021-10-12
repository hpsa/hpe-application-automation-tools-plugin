if (window.NodeList && !NodeList.prototype.forEach) {
	NodeList.prototype.forEach = Array.prototype.forEach;
}
const CredScope = {JOB : "JOB", SYSTEM : "SYSTEM"};

function setupAlmCredentials() {
	let divMain = null;
	const selector = 'div[name="builder"][descriptorid="com.microfocus.application.automation.tools.run.RunFromAlmBuilder"]';
	if (document.currentScript) {
		divMain = document.currentScript.parentElement.closest(selector);
	} else { // IE does not support document.currentScript
		let divs = document.querySelectorAll(selector);
		divMain = divs[divs.length-1];
	}
	setTimeout( function() {
		prepareTask(divMain)}, 200);
}
function prepareTask(divMain) {
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
			const btnUpdate = div.querySelector("input.hidden-password-update-btn");
			btnUpdate && btnUpdate.click();
		});
		[divJobAlmClientId, divJobAlmUsername, divJobAlmSecret, divJobAlmPwd].forEach(function(div) {
			const input = div.querySelector("input");
			input.value = "";
			input.dispatchEvent(evt);
		});
	}
}
