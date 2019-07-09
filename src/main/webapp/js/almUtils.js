document.addEventListener('DOMContentLoaded', function() {
    var ssoCheckbox = document.getElementById('ssoCheckbox');
    enableSSO(ssoCheckbox);
})

function enableSSO(element){
    var username = document.getElementsByName('runfromalm.almUserName')[0];
    var password = document.getElementsByName('runfromalm.almPassword')[0];
    if(element.checked){
        //disable username and password for ALM
        username.disabled = true;
        password.disabled = true;
    } else {
        username.disabled = false;
        password.disabled = false;
    }
}