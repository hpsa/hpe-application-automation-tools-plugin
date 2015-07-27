[@ww.select labelKey="AlmLabEnvPrepareTask.almServerInputLbl" name="almServer" list=uiConfigBean.getExecutableLabels('hpAlmServer') extraUtility=addExecutableLink/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.userNameInputLbl" name="userName"/]
[@ww.password labelKey="AlmLabEnvPrepareTask.passwordInputLbl" name="password" showPassword="false"/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.domainInputLbl" name="domain"/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.projectInputLbl" name="project"/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.AUTEnvIDInputLbl" name="AUTEnvID"/]

[@ww.text name="AlmLabEnvPrepareTask.AUTEnvConfInputLbl"/]
[@ww.select name="envConfig" list="envConfigItems" emptyOption="false"/]
[@ww.textfield name="envConfValue"/]

[@ww.textfield labelKey="AlmLabEnvPrepareTask.pathToJSONFileInputLbl" name="pathToJSONFile"/]
[@ww.textfield labelKey="AlmLabEnvPrepareTask.assignAUTEnvConfIDtoInputLbl" name="assignAUTEnvConfIDto"/]
[@ww.button labelKey="AlmLabEnvPrepareTask.AUTEnvParametersInputLbl" name="AUTEnvParameters"/]