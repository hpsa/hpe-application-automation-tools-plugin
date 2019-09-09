document.addEventListener('DOMContentLoaded', function() {
    var ssoCheckbox = document.getElementById('ssoCheckbox');
    enableSSO(ssoCheckbox);
    validateParameters(ssoCheckbox);
})

function enableSSO(element){
    var username = document.getElementsByName('runfromalm.almUserName')[0];
    var password = document.getElementsByName('runfromalm.almPassword')[0];
    var clientID = document.getElementsByName('runfromalm.almClientID')[0];
    var apiKey = document.getElementsByName('runfromalm.almApiKey')[0];

    if(element.checked){
        //disable username and password for ALM
        username.disabled = true;
        password.disabled = true;
        clientID.disabled = false;
        apiKey.disabled = false;
    } else {
        username.disabled = false;
        password.disabled = false;
        clientID.disabled = true;
        apiKey.disabled = true;
    }
    validateParameters(element);
}

function validateParameters(element){
    var almClientID = document.getElementsByName('runfromalm.almClientID')[0];
    var almApiKey = document.getElementsByName('runfromalm.almApiKey')[0];
    var almUsername = document.getElementsByName('runfromalm.almUserName')[0];

    var errClientIDLabel = document.getElementById('errClientID');
    var errApiKeyLabel = document.getElementById('errApiKey');
    var errUsernameLabel = document.getElementById('errUsername');

    if(almClientID.value == "" && element.checked){
        errClientIDLabel.style.visibility = 'visible';
    } else {
        errClientIDLabel.style.visibility = 'hidden';
    }
    if(almApiKey.value == "" && element.checked){
        errApiKeyLabel.style.visibility = 'visible';
    } else {
        errApiKeyLabel.style.visibility = 'hidden';
    }
    if(almUsername.value == "" && !element.checked){
        errUsernameLabel.style.visibility = 'visible';
    } else {
        errUsernameLabel.style.visibility = 'hidden';
    }
}

function validateField(){
    var ssoCheckbox = document.getElementById('ssoCheckbox');
    validateParameters(ssoCheckbox);
}
