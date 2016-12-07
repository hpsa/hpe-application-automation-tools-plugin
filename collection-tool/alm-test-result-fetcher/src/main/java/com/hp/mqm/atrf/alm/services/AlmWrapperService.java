package com.hp.mqm.atrf.alm.services;

import com.hp.mqm.atrf.alm.core.AlmEntity;
import com.hp.mqm.atrf.alm.entities.*;
import com.hp.mqm.atrf.alm.services.querybuilder.QueryBuilder;
import com.hp.mqm.atrf.core.*;
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

    public void init(FetchConfiguration configuration) {
        System.out.println("Starting fetch process");

        QueryBuilder queryBuilder = buildRunFilter(configuration);
        System.out.print("Fetch runs : ");
        this.runs = fetchRunsREST(queryBuilder);
        System.out.println(" , " + runs.size() + " fetched");

        System.out.print("Fetch tests");
        Set<String> testsIds = fetchTests();
        System.out.println(" " + testsIds.size() + " fetched");

        System.out.print("Fetch test sets");
        Set<String> testSetIds = fetchTestSets();
        System.out.println(" " + testSetIds.size() + " fetched");

        System.out.print("Fetch test configs");
        Set<String> testConfigsIds = fetchTestConfigurations();
        System.out.println(" " + testConfigsIds.size() + " test configs");

        System.out.print("Fetch sprints");
        Set<String> sprintIds = fetchSprints();
        System.out.println(" " + sprintIds.size() + " fetched");

        System.out.print("Fetch releases");
        Set<String> releaseIds = fetchReleases();
        System.out.println(" " + releaseIds.size() + " releases");


        List<NgaInjectionEntity> runsForInjection = prepareRunsForInjection();


        // this._tests = Test.createTestsWithRuns(this._runs);
        // logger.info(String.format("#of alm tests fetched: %d", this._tests.size()));

        // logger.info(String.format("#of alm runs fetched: %d", this._runs.size()));

        System.out.println("\nFetching from alm is done.");
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
            List<AlmEntity> myTests = almEntityService.getEntitiesByIds(Test.COLLECTION_NAME, ids);
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
            List<AlmEntity> mySprints = almEntityService.getEntitiesByIds(Sprint.COLLECTION_NAME, ids);
            for (AlmEntity e : mySprints) {
                sprints.put(e.getId(), (Sprint) e);
            }
        }

        return ids;
    }

    private Set<String> fetchTestConfigurations() {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_TEST_CONFIG_ID, testConfigurations.keySet());
        if (!ids.isEmpty()) {
            List<AlmEntity> myTestConfigs = almEntityService.getEntitiesByIds(TestConfiguration.COLLECTION_NAME, ids);
            for (AlmEntity e : myTestConfigs) {
                testConfigurations.put(e.getId(), (TestConfiguration) e);
            }
        }

        return ids;
    }

    private Set<String> fetchTestSets() {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_TEST_SET_ID, testSets.keySet());
        List<AlmEntity> myTestSets = almEntityService.getEntitiesByIds(TestSet.COLLECTION_NAME, ids);
        for (AlmEntity e : myTestSets) {
            testSets.put(e.getId(), (TestSet) e);
        }
        return ids;
    }


    public Set<String> fetchReleases() {

        Set<String> ids = getIdsNotIncludedInSet(sprints.values(), Sprint.FIELD_RELEASE_ID, releases.keySet());
        List<AlmEntity> myReleases = almEntityService.getEntitiesByIds(Release.COLLECTION_NAME, ids);
        for (AlmEntity e : myReleases) {
            releases.put(e.getId(), (Release) e);
        }
        return ids;
    }


    public List<Run> fetchRunsREST(QueryBuilder queryBuilder) { // maxPages = -1 --> fetch all runs

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

        List<AlmEntity> entities = almEntityService.getAllPagedEntities(Run.COLLECTION_NAME, qb, 1000);
        for (AlmEntity entity : entities) {
            runs.add((Run) entity);
        }

        return runs;
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
