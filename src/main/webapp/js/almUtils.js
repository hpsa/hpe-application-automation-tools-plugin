if (window.NodeList && !NodeList.prototype.forEach) {
	NodeList.prototype.forEach = Array.prototype.forEach;
}
function isReady() {
	setTimeout(function() {
		prepareTask()}, 200);
}
function prepareTask() {
	const isSSOEnabledNodes = document.querySelectorAll('input[type="checkbox"][name="runfromalm.isSSOEnabled"]');
	const noSSOCredentialsNodes = document.querySelectorAll('div.noSSOCredentials');
	const ssoCredentialsNodes = document.querySelectorAll('div.ssoCredentials');

	validateTaskForm();
	isSSOEnabledNodes.forEach(function(checkbox, index) {
		if (checkbox.onclick == null) {
			checkbox.onclick = function() {
				selectCredentialsType(this.checked, index);
			}
		}
	});
	function selectCredentialsType(checked, index) {
		noSSOCredentialsNodes[index].style.display = checked ? "none" : "block";
		ssoCredentialsNodes[index].style.display = checked ? "block" : "none";
	}
	function validateTaskForm() {
		for(let i = 0; i < isSSOEnabledNodes.length; i++) {
			selectCredentialsType(isSSOEnabledNodes[i].checked, i);
		}
	}
}