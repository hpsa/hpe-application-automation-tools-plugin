

package com.hpe.application.automation.bamboo.tasks;

import java.lang.Object;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
import org.apache.commons.lang.StringUtils;


public class UploadApplicationTaskConfigurator extends AbstractLauncherTaskConfigurator {

    public static final String MCSERVERURL = "mcServerURLInput";
    public static final String MCUSERNAME = "mcUserNameInput";
    public static final String MCPASSWORD = "mcPasswordInput";
    public static final String MCAPPLICATIONPATH = "mcAppPath";

    public static final String TASK_NAME_VALUE = "UploadApplicationTask.taskName";
    private static final String TASK_ID_CONTROL = "UploadApplicationTask.taskId";
    private static final String TASK_ID_LBL = "CommonTask.taskIdLbl";

    //proxy type
    public static final String USE_PROXY = "useProxy";
    public static final String SPECIFY_AUTHERATION = "specifyAutheration";

    //proxy info
    public static final String PROXY_ADDRESS = "proxyAddress";
    public static final String PROXY_USERNAME = "proxyUserName";
    public static final String PROXY_PASSWORD = "proxyPassword";

    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(MCSERVERURL, params.getString(MCSERVERURL));
        config.put(MCUSERNAME, params.getString(MCUSERNAME));
        config.put(MCPASSWORD, params.getString(MCPASSWORD));
        config.put(CommonTaskConfigurationProperties.TASK_NAME, getI18nBean().getText(TASK_NAME_VALUE));


        config.put(USE_PROXY, params.getString(USE_PROXY));

        config.put(PROXY_ADDRESS, params.getString(PROXY_ADDRESS));
        config.put(PROXY_USERNAME, params.getString(PROXY_USERNAME));
        config.put(PROXY_PASSWORD, params.getString(PROXY_PASSWORD));

        String[] typesArr = params.getStringArray(MCAPPLICATIONPATH);

        if (typesArr != null) {
            for (int i = 0; i < typesArr.length; ++i) {

                if(StringUtils.isEmpty(typesArr[i])){
                    continue;
                }

                config.put("appPath_" + i,typesArr[i]);
            }
        }

        return config;
    }

    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {

        super.populateContextForCreate(context);

        populateContextForLists(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        final Map<String, String> configuration = taskDefinition.getConfiguration();

        context.put(MCSERVERURL, taskDefinition.getConfiguration().get(MCSERVERURL));
        context.put(MCUSERNAME, taskDefinition.getConfiguration().get(MCUSERNAME));
        context.put(MCPASSWORD, taskDefinition.getConfiguration().get(MCPASSWORD));
        context.put(MCAPPLICATIONPATH, taskDefinition.getConfiguration().get(MCAPPLICATIONPATH));
        context.put(TASK_ID_CONTROL, getI18nBean().getText(TASK_ID_LBL) + String.format("%03d", taskDefinition.getId()));

        context.put(USE_PROXY, taskDefinition.getConfiguration().get(USE_PROXY));
        context.put(PROXY_ADDRESS, taskDefinition.getConfiguration().get(PROXY_ADDRESS));
        context.put(PROXY_USERNAME, taskDefinition.getConfiguration().get(PROXY_USERNAME));
        context.put(PROXY_PASSWORD, taskDefinition.getConfiguration().get(PROXY_PASSWORD));

        List<String> pathList = fetchMCApplicationPathFromContext(configuration);
        context.put("mcPathParams",pathList);
    }

    public static List<String> fetchMCApplicationPathFromContext(@NotNull final Map<String, String> context) {

        List<String> mcPathList = new ArrayList<String>(context.size());

        for(String key: context.keySet())
        {
            if(key.startsWith("appPath_"))
            {
                String path = context.get(key);
                mcPathList.add(path);
            }
        }

        return mcPathList;
    }


    private void populateContextForLists(@org.jetbrains.annotations.NotNull final Map<String, Object> context) {

    }

}