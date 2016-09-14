<html>
<head>
    <title>Configure HP ALM Octane</title>
    <meta name="decorator" content="adminpage">
</head>
<body>
<!--<img src="${req.contextPath}/download/resources/alm-octane-logo.png" border="0"/>-->
<h1>HP ALM Octane Configuration</h1>

<div class="paddedClearer"></div>
    [@ww.form action="/admin/nga/configureOctaneSave.action"
        id="octaneConfigurationForm"
        submitLabelKey='global.buttons.update'
        cancelUri='/admin/administer.action']

        [@ui.bambooSection title="Credentials"]
            [@ww.textfield name='ngaUrl' label='Octane Instance URL' /]
            [@ww.textfield name="apiKey" label='Access Key' /]
            [@ww.textfield name="apiSecret" label='API Secret' /]
            [@ww.textfield name="userToUse" label='Username to use' /]
        [/@ui.bambooSection]
    [/@ww.form]
</body>
</html>