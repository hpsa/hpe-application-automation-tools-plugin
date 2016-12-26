package com.hp.mqm.atrf.alm.services;

import com.hp.mqm.atrf.alm.core.AlmEntity;
import com.hp.mqm.atrf.alm.entities.*;
import com.hp.mqm.atrf.core.configuration.ConfigurationUtilities;
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

    TestFolder unattachedTestFolder;

    AlmEntityService almEntityService;

    public AlmWrapperService(String almBaseUrl, String domain, String project) {

        RestConnector restConnector = new RestConnector();
        restConnector.setBaseUrl(almBaseUrl);

        almEntityService = new AlmEntityService(restConnector);
        almEntityService.setDomain(domain);
        almEntityService.setProject(project);
    }

    public AlmQueryBuilder buildRunFilter(FetchConfiguration configuration) {
        AlmQueryBuilder qb = AlmQueryBuilder.create();
        //StartFromId
        if (StringUtils.isNotEmpty(configuration.getAlmRunFilterStartFromId())) {
            int startFromId = 0;
            if (FetchConfiguration.ALM_RUN_FILTER_START_FROM_ID_LAST_SENT.equals(configuration.getAlmRunFilterStartFromId())) {
                String lastSentRunIdStr = ConfigurationUtilities.readLastSentRunId();
                int lastSentRunId = 0;
                if (StringUtils.isNotEmpty(lastSentRunIdStr)) {
                    try {
                        lastSentRunId = Integer.parseInt(lastSentRunIdStr);
                    } catch (NumberFormatException e) {

                    }
                }
                if (lastSentRunId > 0) {
                    startFromId = lastSentRunId + 1;
                    logger.info(String.format("Last sent run id is %s", lastSentRunId));
                } else {
                    logger.warn(String.format("Valid last sent run id is not found, filtering by lastSentId is ignored"));
                }

            } else {
                startFromId = Integer.parseInt(configuration.getAlmRunFilterStartFromId());
            }
            if (startFromId > 0) {
                qb.addQueryCondition("id", ">=" + startFromId);
            }
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
                AlmQueryBuilder sprintQb = AlmQueryBuilder.create().addQueryCondition(Sprint.FIELD_PARENT_ID, configuration.getAlmRunFilterRelatedEntityId()).addSelectedFields("id");
                List<AlmEntity> sprints = almEntityService.getAllPagedEntities(Sprint.COLLECTION_NAME, sprintQb);
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

    private List<AlmEntity> fetchTests(Collection<Run> runs) {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_TEST_ID, tests.keySet());
        List<AlmEntity> myTests = Collections.emptyList();
        if (!ids.isEmpty()) {
            List<String> fields = Arrays.asList(Test.FIELD_NAME, Test.FIELD_PARENT_ID, Test.FIELD_SUBTYPE);
            myTests = almEntityService.getEntitiesByIds(Test.COLLECTION_NAME, ids, fields);
            for (AlmEntity test : myTests) {
                tests.put(test.getId(), (Test) test);
            }
        }

        return myTests;
    }

    public List<AlmEntity> fetchTestFolders(Collection<AlmEntity> tests) {
        Set<String> ids = getIdsNotIncludedInSet(tests, Test.FIELD_PARENT_ID, testFolders.keySet());
        List<AlmEntity> myTestFolders = Collections.emptyList();
        if (!ids.isEmpty()) {
            List<String> fields = Arrays.asList(TestFolder.FIELD_NAME);
            myTestFolders = almEntityService.getEntitiesByIds(TestFolder.COLLECTION_NAME, ids, fields);
            for (AlmEntity e : myTestFolders) {
                testFolders.put(e.getId(), (TestFolder) e);
            }
        }
        return myTestFolders;
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

    private Set<String> fetchSprints(Collection<Run> runs) {
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

    private Set<String> fetchTestConfigurations(Collection<Run> runs) {
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

    private List<AlmEntity> fetchTestSets(Collection<Run> runs) {
        Set<String> ids = getIdsNotIncludedInSet(runs, Run.FIELD_TEST_SET_ID, testSets.keySet());
        List<AlmEntity> myTestSets = Collections.emptyList();
        if (!ids.isEmpty()) {
            List<String> fields = Arrays.asList(TestSet.FIELD_NAME);
            myTestSets = almEntityService.getEntitiesByIds(TestSet.COLLECTION_NAME, ids, fields);
            for (AlmEntity e : myTestSets) {
                testSets.put(e.getId(), (TestSet) e);
            }

        }
        return myTestSets;
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

    public List<Run> fetchRuns(AlmQueryBuilder queryBuilder) { // maxPages = -1 --> fetch all runs

        List<Run> runs = new ArrayList<>();
        AlmQueryBuilder qb = queryBuilder.clone();
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
                Run.FIELD_TEST_SET_ID,
                Run.FIELD_TEST_CONFIG_ID
        );

        List<AlmEntity> entities = almEntityService.getEntities(Run.COLLECTION_NAME, qb).getEntities();
        for (AlmEntity entity : entities) {
            runs.add((Run) entity);

        }

        return runs;
    }

    public void fetchRunRelatedEntities(List<Run> runs) {
        //clear cache maps
        clearMapIfSizeIsExceed(tests, 4000);
        if(clearMapIfSizeIsExceed(testFolders, 3000)){
            tests.clear();
        }
        clearMapIfSizeIsExceed(testSets, 3000);
        clearMapIfSizeIsExceed(testConfigurations, 4000);

        //fill cache maps
        List<AlmEntity> tests = fetchTests(runs);
        fetchTestFolders(tests);
        fetchTestSets(runs);
        fetchTestConfigurations(runs);
    }

    private boolean clearMapIfSizeIsExceed(Map map, int maxSize) {
        if (map.size() > maxSize) {
            map.clear();
            return true;
        }
        return false;
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
        //Add synthetic data
        if (key.equals("-2")) {
            if (unattachedTestFolder == null) {
                unattachedTestFolder = new TestFolder();
                unattachedTestFolder.put(TestFolder.FIELD_ID, "-2");
                unattachedTestFolder.put(TestFolder.FIELD_NAME, "Unattached");
            }
            return unattachedTestFolder;
        }


        return testFolders.get(key);
    }

    public Test getTest(String key) {
        return tests.get(key);
    }

    public TestConfiguration getTestConfiguration(String key) {
        return testConfigurations.get(key);
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
