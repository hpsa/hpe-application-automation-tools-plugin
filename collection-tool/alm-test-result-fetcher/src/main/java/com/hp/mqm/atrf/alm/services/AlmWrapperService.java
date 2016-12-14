package com.hp.mqm.atrf.alm.services;

import com.hp.mqm.atrf.alm.core.AlmEntity;
import com.hp.mqm.atrf.alm.entities.*;
import com.hp.mqm.atrf.core.configuration.FetchConfiguration;
import com.hp.mqm.atrf.core.rest.RestConnector;
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
    private Map<String, TestFolder> testFolders = new HashMap<>();
    private Map<String, TestConfiguration> testConfigurations = new HashMap<>();
    private List<Run> runs = new ArrayList<>();

    AlmEntityService almEntityService;

    public AlmWrapperService(String almBaseUrl, String domain, String project) {

        RestConnector restConnector = new RestConnector();
        restConnector.setBaseUrl(almBaseUrl);

        almEntityService = new AlmEntityService(restConnector);
        almEntityService.setDomain(domain);
        almEntityService.setProject(project);

        //Add synthetic data
        TestFolder unattachedTestFolder = new TestFolder();
        unattachedTestFolder.put(TestFolder.FIELD_ID, "-2");
        unattachedTestFolder.put(TestFolder.FIELD_NAME, "Unattached");
        testFolders.put(unattachedTestFolder.getId(), unattachedTestFolder);
    }

    public void fetchRunsAndRelatedEntities(FetchConfiguration configuration) {
        logger.info("Starting fetch process from ALM");

        AlmQueryBuilder queryBuilder = buildRunFilter(configuration);

        long start, end, globalStart, globalEnd;

        globalStart = System.currentTimeMillis();
        int expectedRuns = getExpectedRuns(queryBuilder);
        int fetchLimit = Integer.valueOf(configuration.getRunFilterFetchLimit());
        logger.info(String.format("Expected runs : %d", Math.min(expectedRuns, fetchLimit)));

        start = System.currentTimeMillis();
        this.runs = fetchRuns(queryBuilder, fetchLimit);
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch runs : %d, total time %d ms", runs.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> testsIds = fetchTests();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch tests : %d, total time %d ms", testsIds.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> testFoldersIds = fetchTestFolders();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch test folders : %d, total time %d ms", testFoldersIds.size(), end - start));


        start = System.currentTimeMillis();
        Set<String> testSetIds = fetchTestSets();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch test sets : %d, total time %d ms", testSetIds.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> testConfigsIds = fetchTestConfigurations();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch test configs : %d, total time %d ms", testConfigsIds.size(), end - start));

        /*start = System.currentTimeMillis();
        Set<String> sprintIds = fetchSprints();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch sprints : %d, total time %d ms", sprintIds.size(), end - start));

        start = System.currentTimeMillis();
        Set<String> releaseIds = fetchReleases();
        end = System.currentTimeMillis();
        logger.info(String.format("Fetch releases : %d, total time %d ms", releaseIds.size(), end - start));
        */

        globalEnd = System.currentTimeMillis();
        logger.info(String.format("Fetching from alm is done, total time %d ms", globalEnd - globalStart));
    }

    private AlmQueryBuilder buildRunFilter(FetchConfiguration configuration) {
        AlmQueryBuilder qb = AlmQueryBuilder.create();
        //StartFromId
        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterStartFromId())) {
            int startFromId = Integer.parseInt(configuration.getAlmRunFilterStartFromId());
            if (startFromId > 0) {
                qb.addQueryCondition("id", ">=" + startFromId);
            }
        }

        //StartFromDate
        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterStartFromDate())) {
            qb.addQueryCondition("execution-date", ">=" + configuration.getAlmRunFilterStartFromDate());
        }
        //TestType
        boolean supportManual = Boolean.valueOf(configuration.getRunFilterSupportManual());

        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterTestType())) {
            qb.addQueryCondition("test.subtype-id", configuration.getAlmRunFilterTestType() + (supportManual ? "" : " AND <>MANUAL"));
        } else {
            if (!supportManual) {
                qb.addQueryCondition("test.subtype-id", "<>MANUAL");
            }
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
                AlmQueryBuilder sprintQb = AlmQueryBuilder.create().addQueryCondition(Sprint.FIELD_PARENT_ID, configuration.getAlmRunFilterRelatedEntityId()).addSelectedFields("id");
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
            qb.addQueryCondition(AlmQueryBuilder.PREPARED_FILTER, configuration.getAlmRunFilterCustom());
        }


        return qb;
    }

    private Set<String> fetchTests() {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_TEST_ID, tests.keySet());
        if (!ids.isEmpty()) {
            List<String> fields = Arrays.asList(Test.FIELD_NAME, Test.FIELD_PARENT_ID, Test.FIELD_SUBTYPE);
            List<AlmEntity> myTests = almEntityService.getEntitiesByIds(Test.COLLECTION_NAME, ids, fields);
            for (AlmEntity test : myTests) {
                tests.put(test.getId(), (Test) test);
            }
        }

        return ids;
    }

    public Set<String> fetchTestFolders() {

        Set<String> ids = getIdsNotIncludedInSet(tests.values(), Test.FIELD_PARENT_ID, testFolders.keySet());
        List<String> fields = Arrays.asList(TestFolder.FIELD_NAME);
        List<AlmEntity> myTestFolders = almEntityService.getEntitiesByIds(TestFolder.COLLECTION_NAME, ids, fields);
        for (AlmEntity e : myTestFolders) {
            testFolders.put(e.getId(), (TestFolder) e);
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
            List<String> fields = Arrays.asList(Sprint.FIELD_PARENT_ID);
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

        Set<String> ids = getIdsNotIncludedInSet(sprints.values(), Sprint.FIELD_PARENT_ID, releases.keySet());
        List<String> fields = Arrays.asList(Release.FIELD_NAME);
        List<AlmEntity> myReleases = almEntityService.getEntitiesByIds(Release.COLLECTION_NAME, ids, fields);
        for (AlmEntity e : myReleases) {
            releases.put(e.getId(), (Release) e);
        }
        return ids;
    }

    public List<Run> fetchRuns(AlmQueryBuilder queryBuilder, int fetchLimit) { // maxPages = -1 --> fetch all runs

        AlmQueryBuilder qb = AlmQueryBuilder.create();
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

        int maxPages = fetchLimit / AlmEntityService.PAGE_SIZE + 1;
        List<AlmEntity> entities = almEntityService.getAllPagedEntities(Run.COLLECTION_NAME, qb, maxPages);
        for (AlmEntity entity : entities) {
            runs.add((Run) entity);
            if (runs.size() >= fetchLimit) {
                break;
            }
        }

        return runs;
    }

    public int getExpectedRuns(AlmQueryBuilder queryBuilder) {
        return almEntityService.getTotalNumber(Run.COLLECTION_NAME, queryBuilder);
    }

    public boolean login(String user, String password) {
        return almEntityService.login(user, password);
    }

    public boolean validateConnectionToProject() {
        try {
            //try to get resource, if succeeded - the connection is valid
            AlmQueryBuilder qb = AlmQueryBuilder.create().addQueryCondition("id", "0");
            almEntityService.getTotalNumber(Test.COLLECTION_NAME, qb);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Release getRelease(String key) {
        return releases.get(key);
    }

    public TestSet getTestSet(String key) {
        return testSets.get(key);
    }

    public Sprint getSprint(String key) {
        return sprints.get(key);
    }


    public TestFolder getTestFolder(String key) {
        return testFolders.get(key);
    }

    public Test getTest(String key) {
        return tests.get(key);
    }

    public TestConfiguration getTestConfiguration(String key) {
        return testConfigurations.get(key);
    }

    public List<Run> getRuns() {
        return runs;
    }

    public String getDomain() {
        return almEntityService.getDomain();
    }

    public String getProject() {
        return almEntityService.getProject();
    }

    public String generateALMReferenceURL(AlmEntity entity) {
        return almEntityService.generateALMReferenceURL(entity);
    }

}
