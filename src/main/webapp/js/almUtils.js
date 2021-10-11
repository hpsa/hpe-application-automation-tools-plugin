if (window.NodeList && !NodeList.prototype.forEach) {
	NodeList.prototype.forEach = Array.prototype.forEach;
}
const CredScope = {JOB : "JOB", SYSTEM : "SYSTEM"};

function setupAlmCredentials() {
	setTimeout(function() {
		prepareTask()}, 200);
}
function prepareTask() {
	const credentialsScopeNodes = document.querySelectorAll('select[name="almCredentialsScope"]');
	const isSSOEnabledNodes = document.querySelectorAll('input[type="checkbox"][name="runfromalm.isSSOEnabled"]');
	const sysAlmUsernameNodes = document.querySelectorAll('div.sys-alm-username');
	const sysAlmClientIdNodes = document.querySelectorAll('div.sys-alm-client-id');
	const jobAlmUsernameNodes = document.querySelectorAll('div.job-alm-username');
	const jobAlmPwdNodes = document.querySelectorAll('div.job-alm-password');
	const jobAlmCIDNodes = document.querySelectorAll('div.job-alm-client-id');
	const jobAlmApiKeyNodes = document.querySelectorAll('div.job-alm-secret');

	isSSOEnabledNodes.forEach(function(checkbox, index) {
		selectCredentialsType(checkbox.checked, index, credentialsScopeNodes[index].value);
		if (typeof checkbox.onclick !== "function") {
			checkbox.onclick = function() {
				selectCredentialsType(this.checked, index, credentialsScopeNodes[index].value);
			}
		}
	});
	credentialsScopeNodes.forEach(function(dropdown, idx) {
		credentialsScopeChanged(dropdown.value, idx);
		if (typeof dropdown.onchange !== "function") {
			dropdown.onchange = function() {
				credentialsScopeChanged(this.value, idx);
			}
		}
	});

	function selectCredentialsType(isSSO, idx, scope) {
		const isJobScope = scope === CredScope.JOB;
		const jobAlmUserNode = jobAlmUsernameNodes[idx];
		const jobAlmPwdNode = jobAlmPwdNodes[idx];
		const jobAlmCIDNode = jobAlmCIDNodes[idx];
		const jobAlmApiKeyNode = jobAlmApiKeyNodes[idx];
		const sysAlmUserNode = sysAlmUsernameNodes[idx];
		const sysAlmCIDNode = sysAlmClientIdNodes[idx];
		if (isJobScope) {
			sysAlmUserNode.querySelector("select").name += "_x";
			sysAlmCIDNode.querySelector("select").name += "_x";
			sysAlmUsernameNodes[idx].style.display = sysAlmClientIdNodes[idx].style.display = "none";
			jobAlmUserNode.querySelector("input").name = "almUserName";
			jobAlmPwdNode.querySelector("input").name = "almPassword";
			jobAlmCIDNode.querySelector("input").name = "almClientID";
			jobAlmApiKeyNode.querySelector("input").name = "almApiKey";
			jobAlmUsernameNodes[idx].style.display = jobAlmPwdNodes[idx].style.display = isSSO ? "none" : "block";
			jobAlmCIDNodes[idx].style.display = jobAlmApiKeyNodes[idx].style.display = isSSO ? "block" : "none";
		} else {
			sysAlmUserNode.querySelector("select").name = "almUserName";
			sysAlmCIDNode.querySelector("select").name = "almClientID";
			jobAlmUserNode.querySelector("input").name += "_x";
			jobAlmCIDNode.querySelector("input").name += "_x";
			jobAlmPwdNode.querySelector("input").name += "_x";
			jobAlmApiKeyNode.querySelector("input").name += "_x";
			jobAlmUsernameNodes[idx].style.display = jobAlmPwdNodes[idx].style.display = jobAlmCIDNodes[idx].style.display = jobAlmApiKeyNodes[idx].style.display = "none";
			sysAlmUsernameNodes[idx].style.display = isSSO ? "none" : "block";
			sysAlmClientIdNodes[idx].style.display = isSSO ? "block" : "none";
		}
	}

	function credentialsScopeChanged(scope, idx) {
		const isSSO = isSSOEnabledNodes[idx].checked;
		selectCredentialsType(isSSO, idx, scope);
	}
}
