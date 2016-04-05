::copy the plug-in under TeamCity plugins directory and restart the server

set PLUGIN_PATH=%~dp0\..\..\target\hp-lifecycle-management-teamcity-ci-plugin.zip
set TEAMCITY_PLUGINS_DIR=C:\ProgramData\JetBrains\TeamCity\plugins

xcopy %PLUGIN_PATH% %TEAMCITY_PLUGINS_DIR% /y
setx TEAMCITY_SERVER_OPTS "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5000 -Dhttps.proxyHost=web-proxy.il.hpecorp.net -Dhttps.proxyPort=8080 -Dhttp.proxyHost=web-proxy.il.hpecorp.net -Dhttp.proxyPort=8080"

net stop TeamCity
net start TeamCity
