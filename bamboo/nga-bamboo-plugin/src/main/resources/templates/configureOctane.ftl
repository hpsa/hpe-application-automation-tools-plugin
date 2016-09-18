<html>
<head>
    <title>Configure HPE Octane Plugin</title>
    <meta name="decorator" content="adminpage">
</head>
<body>
<!--<img src="${req.contextPath}/download/resources/alm-octane-logo.png" border="0"/>-->
<h1>HPE ALM Octane CI Plugin Configuration</h1>

<div class="paddedClearer"></div>
    [@ww.form action="/admin/nga/configureOctaneSave.action"
        id="octaneConfigurationForm"
        submitLabelKey='global.buttons.update'
        cancelUri='/admin/administer.action']

        [@ui.bambooSection title="Credentials"]
            [@ww.textfield name='octaneUrl' label='Octane Instance URL' /]
            [@ww.textfield name="accessKey" label='Access Key' /]
            [@ww.textfield name="apiSecret" label='API Secret' /]
            [@ww.textfield name="userName" label='Username to use' /]
        [/@ui.bambooSection]
    [/@ww.form]
</body>
</html>