set PLUGIN_PATH=C:\dev\indi-integrations\teamcity\target\hp-lifecycle-management-teamcity-ci-plugin.zip
set TEAMCITY_PLUGINS_DIR=C:\ProgramData\JetBrains\TeamCity\plugins

xcopy %PLUGIN_PATH% %TEAMCITY_PLUGINS_DIR% /y
setx TEAMCITY_SERVER_OPTS "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5000"

net stop TeamCity
net start TeamCity

net stop TCBuildAgent
net start TCBuildAgent