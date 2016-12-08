package com.hp.mqm.atrf.alm.services;

import com.hp.mqm.atrf.alm.core.AlmEntity;
import com.hp.mqm.atrf.alm.entities.*;
import com.hp.mqm.atrf.alm.services.querybuilder.QueryBuilder;
import com.hp.mqm.atrf.core.configuration.FetchConfiguration;
import com.hp.mqm.atrf.core.rest.RestConnector;
import com.hp.mqm.atrf.octane.entities.NgaInjectionEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


/**
 * Created by bennun on 27/06/2016.
 */
public class AlmWrapperService {
    static final Logger logger = LogManager.getLogger();

    private Map<String, Release> releases = new HashMap<>();
    private Map<String, TestSet> testSets = new HashMap<>();
    private Map<String, Sprint> sprints = new HashMap<>();
    private Map<String, Test> tests = new HashMap<>();
    private Map<String, TestConfiguration> testConfigurations = new HashMap<>();
    private List<Run> runs = new ArrayList<>();
    private Map<String, String> alm2OctaneTestingToolMapper = new HashMap<>();

    RestConnector restConnector;
    AlmEntityService almEntityService;

    public AlmWrapperService(String almBaseUrl, String domain, String project) {

        restConnector = new RestConnector();
        restConnector.setBaseUrl(almBaseUrl);

        almEntityService = new AlmEntityService(restConnector);
        almEntityService.setDomain(domain);
        almEntityService.setProject(project);

        alm2OctaneTestingToolMapper.put("MANUAL", "Manual");
        alm2OctaneTestingToolMapper.put("LEANFT-TEST", "LeanFT");
        alm2OctaneTestingToolMapper.put("QUICKTEST_TEST", "UFT");
        alm2OctaneTestingToolMapper.put("BUSINESS-PROCESS", "BPT");
    }

    public void fetchRunsAndRelatedEntities(FetchConfiguration configuration) {
        logger.info("Starting fetch process from ALM");

        QueryBuilder queryBuilder = buildRunFilter(configuration);

        long start, end, globalStart, globalEnd;

        globalStart = System.currentTimeMillis();
        int expectedRuns = getExpectedRuns(queryBuilder);
        logger.info(String.format("Expected runs : %d", expectedRuns));

        start = System.currentTimeMillis();
        this.runs = fetchRuns(queryBuilder);
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch runs : %d, total time %d ms", runs.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> testsIds = fetchTests();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch tests : %d, total time %d ms", testsIds.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> testSetIds = fetchTestSets();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch test sets : %d, total time %d ms", testSetIds.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> testConfigsIds = fetchTestConfigurations();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch test configs : %d, total time %d ms", testConfigsIds.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> sprintIds = fetchSprints();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch sprints : %d, total time %d ms", sprintIds.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> releaseIds = fetchReleases();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch releases : %d, total time %d ms", releaseIds.size(), end - start));

        globalEnd = System.currentTimeMillis();
        logger.info(String.format("Fetching from alm is done, total time %d ms", globalEnd - globalStart));

        List<NgaInjectionEntity> runsForInjection = prepareRunsForInjection();


    }

    private QueryBuilder buildRunFilter(FetchConfiguration configuration) {
        QueryBuilder qb = QueryBuilder.create();
        //StartFromId
        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterStartFromId())) {
            int startFromId = Integer.parseInt(configuration.getAlmRunFilterStartFromId());
            qb.addQueryCondition("id", ">=" + startFromId);
        }
        //StartFromDate
        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterStartFromDate())) {
            qb.addQueryCondition("execution-date", ">=" + configuration.getAlmRunFilterStartFromDate());
        }
        //TestType
        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterTestType())) {
            qb.addQueryCondition("test.subtype-id", configuration.getAlmRunFilterTestType());
        }
        //RelatedEntity
        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterRelatedEntityType())) {
            Map<String, String> relatedEntity2runFieldMap = new HashMap<>();
            relatedEntity2runFieldMap.put("test", "test-id");
            relatedEntity2runFieldMap.put("testset", "cycle-id");
            relatedEntity2runFieldMap.put("sprint", "assign-rcyc");
            relatedEntity2runFieldMap.put("release", "assign-rcyc");

            String field = relatedEntity2runFieldMap.get(configuration.getAlmRunFilterRelatedEntityType());
            String value = configuration.getAlmRunFilterRelatedEntityId();

            if (configuration.getAlmRunFilterRelatedEntityType().equals("release")) {
                //fetch sprints of the release
                QueryBuilder sprintQb = QueryBuilder.create().addQueryCondition(Sprint.FIELD_RELEASE_ID, configuration.getAlmRunFilterRelatedEntityId()).addSelectedFields("id");
                List<AlmEntity> sprints = almEntityService.getAllPagedEntities(Sprint.COLLECTION_NAME, sprintQb, 1000);
                Set<String> sprintIds = new HashSet<>();
                for (AlmEntity sprint : sprints) {
                    sprintIds.add(sprint.getId());
                }
                if (sprints.isEmpty()) {
                    throw new RuntimeException("Release ID in configuration file, doesn't contains sprints");
                }

                value = StringUtils.join(sprintIds, " OR ");
            }

            qb.addQueryCondition(field, value);
        }
        //custom
        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterCustom())) {
            qb.addQueryCondition(QueryBuilder.PREPARED_FILTER, configuration.getAlmRunFilterCustom());
        }

        return qb;
    }

    private Set<String> fetchTests() {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_TEST_ID, tests.keySet());
        if (!ids.isEmpty()) {
            List<String> fields = Arrays.asList(Test.FIELD_NAME);
            List<AlmEntity> myTests = almEntityService.getEntitiesByIds(Test.COLLECTION_NAME, ids, fields);
            for (AlmEntity test : myTests) {
                tests.put(test.getId(), (Test) test);
            }
        }

        return ids;
    }


    private Set<String> getIdsNotIncludedInSet(Collection<? extends AlmEntity> entities, String keyFieldName, Collection<String> ids) {
        Set<String> notIncludedIds = new HashSet<>();
        for (AlmEntity entity : entities) {
            String id = entity.getString(keyFieldName);
            if (StringUtils.isNotEmpty(id) && !ids.contains(id)) {
                notIncludedIds.add(id);
            }
        }
        return notIncludedIds;
    }

    private Set<String> fetchSprints() {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_SPRINT_ID, sprints.keySet());
        if (!ids.isEmpty()) {
            List<String> fields = Arrays.asList(Sprint.FIELD_RELEASE_ID);
            List<AlmEntity> mySprints = almEntityService.getEntitiesByIds(Sprint.COLLECTION_NAME, ids, fields);
            for (AlmEntity e : mySprints) {
                sprints.put(e.getId(), (Sprint) e);
            }
        }

        return ids;
    }

    private Set<String> fetchTestConfigurations() {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_TEST_CONFIG_ID, testConfigurations.keySet());
        if (!ids.isEmpty()) {
            List<String> fields = Arrays.asList(TestConfiguration.FIELD_NAME);
            List<AlmEntity> myTestConfigs = almEntityService.getEntitiesByIds(TestConfiguration.COLLECTION_NAME, ids, fields);
            for (AlmEntity e : myTestConfigs) {
                testConfigurations.put(e.getId(), (TestConfiguration) e);
            }
        }

        return ids;
    }

    private Set<String> fetchTestSets() {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_TEST_SET_ID, testSets.keySet());
        List<String> fields = Arrays.asList(TestSet.FIELD_NAME);
        List<AlmEntity> myTestSets = almEntityService.getEntitiesByIds(TestSet.COLLECTION_NAME, ids, fields);
        for (AlmEntity e : myTestSets) {
            testSets.put(e.getId(), (TestSet) e);
        }
        return ids;
    }

    public Set<String> fetchReleases() {

        Set<String> ids = getIdsNotIncludedInSet(sprints.values(), Sprint.FIELD_RELEASE_ID, releases.keySet());
        List<String> fields = Arrays.asList(Release.FIELD_NAME);
        List<AlmEntity> myReleases = almEntityService.getEntitiesByIds(Release.COLLECTION_NAME, ids, fields);
        for (AlmEntity e : myReleases) {
            releases.put(e.getId(), (Release) e);
        }
        return ids;
    }


    public List<Run> fetchRuns(QueryBuilder queryBuilder) { // maxPages = -1 --> fetch all runs

        QueryBuilder qb = QueryBuilder.create();
        qb.addOrderBy(Run.FIELD_ID);
        qb.addSelectedFields(
                Run.FIELD_ID,
                Run.FIELD_NAME,
                Run.FIELD_SPRINT_ID,
                Run.FIELD_DURATION,
                Run.FIELD_STATUS,
                Run.FIELD_TYPE,
                Run.FIELD_DATE,
                Run.FIELD_TIME,
                Run.FIELD_TEST_ID,
                Run.FIELD_TEST_INSTANCE_ID,
                Run.FIELD_OS_NAME,
                Run.FIELD_TEST_SET_ID,
                Run.FIELD_DRAFT,
                Run.FIELD_TEST_CONFIG_ID,
                Run.FIELD_EXECUTOR);
        qb.addQueryConditions(queryBuilder.getQueryConditions());

        List<AlmEntity> entities = almEntityService.getAllPagedEntities(Run.COLLECTION_NAME, qb, 10000);
        for (AlmEntity entity : entities) {
            runs.add((Run) entity);
        }

        return runs;
    }

    public int getExpectedRuns(QueryBuilder queryBuilder) {
        return almEntityService.getTotalNumber(Run.COLLECTION_NAME, queryBuilder);
    }

    public boolean login(String user, String password) {
        return almEntityService.login(user, password);
    }

    public boolean validateConnectionToProject() {
        try {
            //try to get resource, if succeeded - the connection is valid
            QueryBuilder qb = QueryBuilder.create().addQueryCondition("id", "0");
            almEntityService.getTotalNumber(Test.COLLECTION_NAME, qb);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<NgaInjectionEntity> prepareRunsForInjection() {
        List<NgaInjectionEntity> list = new ArrayList<>();

        for (Run run : runs) {

            Test test = tests.get(run.getTestId());
            TestSet testSet = testSets.get(run.getTestSetId());
            TestConfiguration testConfiguration = testConfigurations.get(run.getTestConfigId());

            Release release = null;
            if (StringUtils.isNotEmpty(run.getSprintId())) {
                Sprint sprint = sprints.get(run.getSprintId());
                release = releases.get(sprint.getReleaseId());
            }

            /*private String testName;
            private String testingToolType;//test type
            private String testDescription;//test description
            private String packageValue;//project
            private String classValue;//test path (not equal to test URL)
            private String component;//domain
            private String testLastModified;//test last modified

            private String duration;//run duration
            private List<String> environment;//testSet name
            private String externalReportUrl;//run url to alm
            private String runName;//alm Run ID\ alm Run Name
            private String releaseId;
            private String releaseName;
            private String startedTime;
            private String status;
            private String runBy;
            private String draftRun;*/

            NgaInjectionEntity injectionEntity = new NgaInjectionEntity();
            list.add(injectionEntity);

            String testingTool = alm2OctaneTestingToolMapper.get(test.getSubType());
            injectionEntity.setTestingToolType(testingTool);
            injectionEntity.setTestDescription(test.getDescription());
            injectionEntity.setPackageValue(almEntityService.getProject());
            injectionEntity.setComponent(almEntityService.getDomain());
            injectionEntity.setClassValue(test.getId() + "_" + test.getName());

            //test name + test configuration, if Test name =Test configuration, just keep test name
            String testName = testConfiguration.getName().equals(test.getName()) ? test.getName() : test.getName() + "_" + testConfiguration.getName();
            injectionEntity.setTestName(testName);


            injectionEntity.setDuration(run.getDuration());
            Map<String, String> environment = new HashMap<>();
            injectionEntity.setEnvironment(environment);
            environment.put("test-set", testSet.getName());
            if (StringUtils.isNotEmpty(run.getOsName())) {
                environment.put("os", run.getOsName());
            }


            injectionEntity.setExternalReportUrl(almEntityService.generateALMReferenceURL(run));
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


}
