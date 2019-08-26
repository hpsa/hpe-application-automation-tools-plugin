document.addEventListener('DOMContentLoaded', function() {
    var ssoCheckbox = document.getElementById('ssoCheckbox');
    enableSSO(ssoCheckbox);
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
}