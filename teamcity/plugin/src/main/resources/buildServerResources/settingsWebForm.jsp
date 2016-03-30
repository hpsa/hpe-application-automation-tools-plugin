
<html>
<head>
    <script>
        function loadDoc() {
            var xhttp = new XMLHttpRequest();
            xhttp.onreadystatechange = function() {
                if (xhttp.readyState == 4 && xhttp.status == 200) {
                    var obj = JSON.parse(xhttp.responseText);
                    document.getElementById("server").value  = obj.uiLocation;
                    document.getElementById("username1").value  = obj.username;
                    document.getElementById("password1").value  = obj.secretPassword;
                }
            };
            var parameters ="action=reload";
            xhttp.open("GET", "/octane-rest/", true);
            xhttp.send(parameters);
        }
    </script>


    <script>
        function saveParams() {
            var xhttp = new XMLHttpRequest();
            xhttp.onreadystatechange = function() {

                if(xhttp.readyState == 4) {
                    if (xhttp.status == 200)
                        message_box_div.innerHTML = xhttp.responseText;
                    else
                        message_box_div.innerHTML = "Error"
                }else{
                    message_box_div.innerHTML = "Saving...";
                }
            };
            var server= encodeURIComponent(document.getElementById("server").value);
            var username = encodeURIComponent(document.getElementById("username1").value);
            var password =encodeURIComponent(document.getElementById("password1").value);

            var parameters = "action=save"+"&server="+server+"&username1="+username+"&password1="+password;

            xhttp.open("POST", "/octane-rest/", true)
            xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
            xhttp.send(parameters)
        }

    </script>




    <script>
        function checkConnection() {
            var xhttp = new XMLHttpRequest();
            xhttp.onreadystatechange = function() {
                if(xhttp.readyState == 4) {
                    if (xhttp.status == 200)
                        message_box_div.innerHTML = xhttp.responseText;
                    else
                        message_box_div.innerHTML = "Error"
                }else{
                    message_box_div.innerHTML = "Waiting...";
                }
            };
            var server= encodeURIComponent(document.getElementById("server").value);
            var username = encodeURIComponent(document.getElementById("username1").value);
            var password =encodeURIComponent(document.getElementById("password1").value);

            var parameters = "action=test&server="+server+"&username1="+username+"&password1="+password;

            xhttp.open("POST", "/octane-rest/", true)
            xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
            xhttp.send(parameters)
        }

    </script>



</head>
<body>

<div id="settingsContainer">
    <form action="/octane-rest/admin/" method="post" >
        <div class="editNotificatorSettingsPage">




            <table class="runnerFormTable">
                <tr>
                    <th><label for="server">Location <span class="mandatoryAsterix" title="Mandatory field">*</span></label></th>
                    <td>
                        <input type="text" name="server" id="server"   value="" class="longField"        >

                        <span class="error" id="errorServer"></span>
                    </td>
                </tr>


                <tr>
                    <th><label for="username1">Client ID <span class="mandatoryAsterix" title="Mandatory field">*</span></label></th>
                    <td>
                        <input type="text" name="username1" id="username1"   value="" class="longField"        >

                        <span class="error" id="errorUsername1"></span>
                    </td>
                </tr>

                <tr>
                    <th><label for="password1">Client secret <span class="mandatoryAsterix" title="Mandatory field">*</span></label></th>
                    <td>
                        <input type="password" name="password1" id="password1"   value="" class="longField"        >

                        <span class="error" id="errorPassword"></span>


                    </td>
                </tr>




            </table>

            <div class="saveButtonsBlock">


                <input type="button" value="Save" class="btn btn_primary submitButton "   onClick="saveParams()"  />

                <input type="button" value="Test connection" class="btn btn_primary submitButton " id="testConnection"  onClick="checkConnection()"  />

            </div>
        </div>

    </form>
</div>
<div id="message_box_div" class="message_box_div">
</div>
<script>
    loadDoc()
</script>
</body>
</html>