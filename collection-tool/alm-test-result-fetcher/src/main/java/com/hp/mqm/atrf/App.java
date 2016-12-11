package com.hp.mqm.atrf;

import com.hp.mqm.atrf.alm.entities.*;
import com.hp.mqm.atrf.alm.services.AlmWrapperService;
import com.hp.mqm.atrf.core.configuration.FetchConfiguration;
import com.hp.mqm.atrf.octane.entities.NgaInjectionEntity;
import com.hp.mqm.atrf.octane.services.OctaneWrapperService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by berkovir on 08/12/2016.
 */
public class App {
    static final Logger logger = LogManager.getLogger();
    private FetchConfiguration configuration;
    private AlmWrapperService almWrapper;
    private OctaneWrapperService octaneWrapper;

    private Map<String, String> alm2OctaneTestingToolMapper = new HashMap<>();


    public App(FetchConfiguration configuration) {
        this.configuration = configuration;

        alm2OctaneTestingToolMapper.put("MANUAL", "Manual");
        alm2OctaneTestingToolMapper.put("LEANFT-TEST", "LeanFT");
        alm2OctaneTestingToolMapper.put("QUICKTEST_TEST", "UFT");
        alm2OctaneTestingToolMapper.put("BUSINESS-PROCESS", "BPT");
    }

    public void start() {

        loginToAlm();
        loginToOctane();

        almWrapper.fetchRunsAndRelatedEntities(configuration);
        List<NgaInjectionEntity> ngaRuns = prepareRunsForInjection();
    }

    private void loginToAlm() {
        logger.info("ALM : Validating login configuration ...");
        almWrapper = new AlmWrapperService(configuration.getAlmServerUrl(), configuration.getAlmDomain(), configuration.getAlmProject());
        if (almWrapper.login(configuration.getAlmUser(), configuration.getAlmPassword())) {

            logger.info("ALM : Login successful");
            if (almWrapper.validateConnectionToProject()) {
                logger.info("ALM : Connected to ALM project successfully");
            } else {
                throw new RuntimeException("ALM : Failed to connect to ALM Project.");
            }
        } else {
            throw new RuntimeException("ALM : Failed to login");
        }
    }

    private void loginToOctane() {
        logger.info("Octane : Validating login configuration ...");
        long sharedSpaceId = Long.parseLong(configuration.getOctaneSharedSpaceId());
        long workspaceId = Long.parseLong(configuration.getOctaneWorkspaceId());

        octaneWrapper = new OctaneWrapperService(configuration.getOctaneServerUrl(), sharedSpaceId, workspaceId);
        if (octaneWrapper.login(configuration.getOctaneUser(), configuration.getOctanePassword())) {

            logger.info("Octane : Login successful");
            if (octaneWrapper.validateConnectionToWorkspace()) {
                logger.info("Octane : Connected to Octane project successfully");
            } else {
                throw new RuntimeException("Octane : Failed to connect to Octane Workspace.");
            }
        } else {
            throw new RuntimeException("Octane : Failed to login");
        }
    }

    private List<NgaInjectionEntity> prepareRunsForInjection() {
        List<NgaInjectionEntity> list = new ArrayList<>();

        for (Run run : almWrapper.getRuns()) {

            Test test = almWrapper.getTest(run.getTestId());
            TestSet testSet = almWrapper.getTestSet(run.getTestSetId());
            TestConfiguration testConfiguration = almWrapper.getTestConfiguration(run.getTestConfigId());

            Release release = null;
            if (StringUtils.isNotEmpty(run.getSprintId())) {
                Sprint sprint = almWrapper.getSprint(run.getSprintId());
                release = almWrapper.getRelease(sprint.getReleaseId());
            }

            NgaInjectionEntity injectionEntity = new NgaInjectionEntity();
            list.add(injectionEntity);

            //TEST NAME
            //test name + test configuration, if Test name =Test configuration, just keep test name
            String testName = String.format("AlmTestId #%s : %s", test.getId(),test.getName());
            if(!testConfiguration.getName().equals(test.getName())) {
                testName = String.format("AlmTestId #%s, ConfId#%s : %s - %s", test.getId(), testConfiguration.getId(), test.getName(), testConfiguration.getName());
            }
            testName = restrictTo255(testName);
            injectionEntity.setTestName(testName);

            //TESTING TOOL
            String testingTool = alm2OctaneTestingToolMapper.get(test.getSubType());
            injectionEntity.setTestingToolType(testingTool);

            //PACKAGE AND COMPONENT
            injectionEntity.setPackageValue(almWrapper.getProject());
            injectionEntity.setComponent(almWrapper.getDomain());

            //CLASS NAME
            injectionEntity.setClassValue(test.getId() + "_" + test.getName());




            injectionEntity.setDuration(run.getDuration());
            Map<String, String> environment = new HashMap<>();
            injectionEntity.setEnvironment(environment);
            environment.put("test-set", testSet.getName());
            if (StringUtils.isNotEmpty(run.getOsName())) {
                environment.put("os", run.getOsName());
            }


            injectionEntity.setExternalReportUrl(almWrapper.generateALMReferenceURL(run));
            injectionEntity.setRunName(run.getId() + "_" + run.getName());

            if (release != null) {
                injectionEntity.setReleaseId(release.getId());
                injectionEntity.setReleaseName(release.getName());

            }

            injectionEntity.setStartedTime(run.getExecutionDate() + " " + run.getExecutionTime());
            injectionEntity.setStatus(run.getStatus());
            injectionEntity.setRunBy(run.getExecutor());
            injectionEntity.setDraftRun(run.getDraft());
        }

        return list;
    }

    private String restrictTo255(String value) {
        if(value==null || value.length()<=255){
            return value;
        }

        return value.substring(0,255);
    }

}
