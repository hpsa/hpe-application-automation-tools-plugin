package com.hp.octane.integrations.services.predictive;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.spi.CIPluginServices;

import java.io.File;

/**
 * Created by benmeior on 2/1/2017.
 */
public final class PredictiveService extends OctaneSDK.SDKServiceBase {
    private static final Object INIT_LOCKER = new Object();
    private static final String PEM_FILE_SYS_PARAM = "pem_file";
    private static final String PREDICTIVE_PEM_FILE_NAME = "predictive.pem";

    private final CIPluginServices pluginServices;

    public PredictiveService(Object configurator, CIPluginServices pluginServices) {
        super(configurator);

        if (pluginServices == null) {
            throw new IllegalArgumentException("plugin services MUST NOT be null");
        }

        this.pluginServices = pluginServices;
        configurePredictivePemFile();
    }

    private void configurePredictivePemFile() {
        File predictiveOctanePath = pluginServices.getPredictiveOctanePath();
        if (predictiveOctanePath != null && predictiveOctanePath.isDirectory()) {
            synchronized (INIT_LOCKER) {
                String pemFilePath = predictiveOctanePath.getAbsolutePath() + File.separator + PREDICTIVE_PEM_FILE_NAME;
                System.setProperty(PEM_FILE_SYS_PARAM, pemFilePath);
            }
        }
    }
}
