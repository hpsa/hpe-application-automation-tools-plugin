<!--<img src="${req.contextPath}/download/resources/alm-octane-logo.png" border="0"/>-->
<script>
    function testConfiguration() {
        var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                var p = document.getElementById("resultPNode");
                // clear children
                p.textContent ='';
                var t = document.createTextNode(this.responseText);
                if (this.responseText.toLocaleLowerCase().includes("success")) {
                    p.style.color = "green";
                }
                else {
                    p.style.color = "red";
                }
                p.appendChild(t);
            }
        };
        xhttp.open("POST", "${req.contextPath}/rest/octane/1.0/testconnection", true);
        xhttp.setRequestHeader('content-type','application/json');
        xhttp.send(JSON.stringify({
            octaneUrl: document.getElementById("octaneConfigurationForm_octaneUrl").value,
            accessKey: document.getElementById("octaneConfigurationForm_accessKey").value,
            apiSecret: document.getElementById("octaneConfigurationForm_apiSecret").value
        }));
    }
</script>
<h1>HPE ALM Octane CI Plugin Configuration</h1>
<div class="paddedClearer"></div>
[@ww.form action="/admin/nga/configureOctaneSave.action"
id="octaneConfigurationForm"
submitLabelKey='global.buttons.update']
[@ui.bambooSection title="Credentials"]
[@ww.textfield name='octaneUrl' label='Location' /]
[@ww.textfield name="accessKey" label='Client ID' /]
[@ww.textfield name="apiSecret" label='Client secret' /]
[@ww.textfield name="userName" label='Bamboo user' /]
[/@ui.bambooSection]
[/@ww.form]
<script>
    // adding the check connection button
    var buttonsContainer = document.querySelector('#octaneConfigurationForm > .buttons-container > .buttons'),
    testButton = document.createElement('input');
    testButton.type = 'button';
    testButton.value = 'Check Connection';
    testButton.className = 'aui-button aui-button-primary';
    testButton.onclick = testConfiguration;
    buttonsContainer.appendChild(testButton);
    // adding the check result text
    var buttonsContainer = document.querySelector('#octaneConfigurationForm > .buttons-container '),
    pAnswer = document.createElement('p');
    pAnswer.id = "resultPNode";
    buttonsContainer.appendChild(pAnswer);
    var t = document.createTextNode("");
    pAnswer.appendChild(t);
</script>
