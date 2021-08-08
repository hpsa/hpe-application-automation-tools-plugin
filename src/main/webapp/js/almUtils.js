if (window.NodeList && !NodeList.prototype.forEach) {
	NodeList.prototype.forEach = Array.prototype.forEach;
}
function setupSSO() {
	setTimeout(function() {
		prepareTask()}, 200);
}
function prepareTask() {
	const isSSOEnabledNodes = document.querySelectorAll('input[type="checkbox"][name="runfromalm.isSSOEnabled"]');
	const noSSOCredentialsNodes = document.querySelectorAll('div.noSSOCredentials');
	const ssoCredentialsNodes = document.querySelectorAll('div.ssoCredentials');

	isSSOEnabledNodes.forEach(function(checkbox, index) {
		selectCredentialsType(checkbox.checked, index);
		if (typeof checkbox.onclick !== "function") {
			checkbox.onclick = function() {
				selectCredentialsType(this.checked, index);
			}
		}
	});
	function selectCredentialsType(checked, index) {
		noSSOCredentialsNodes[index].style.display = checked ? "none" : "block";
		ssoCredentialsNodes[index].style.display = checked ? "block" : "none";
	}
}