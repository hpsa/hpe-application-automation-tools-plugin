README
======

HPE ALM Test Result Collection Tool
------------------------------------------------------------------------------------------------------------------------

The HPE ALM Test Result Collection Tool is a command line tool for fetching test result from ALM into Octane server.

------------------------------------------------------------------------------------------------------------------------
Usage ******************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------

java -jar alm-test-result-collection-tool.jar [OPTIONS]...


 -c,--config-file <FILE>               configuration file location. Default configuration file name is 'conf.xml'
 -h,--help                             show this help
 -o,--output-file                      write output to file instead of pushing it to the Octane server.
                                       File path is optional. Default file name is 'output.xml'
 -pa,--password-alm <PASSWORD>         password for alm user
 -paf,--password-alm-file <FILE>       location of file with password for alm user
 -po,--password-oct <PASSWORD>         password for octane user
 -pof,--password-oct-file <FILE>       location of file with password for octane user
 -rfd,--run-filter-date <YYYY-MM-DD>   start run fetching from date
 -rfid,--run-filter-id <ID>            start run fetching from id
 -rfl,--run-filter-limit <NUMBER>      limit number of fetched runs from ALM side
 -v,--version                          show version of this tool

------------------------------------------------------------------------------------------------------------------------
Configuration **********************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
To fetch test results from ALM to Octane server, this tool requires information about ALM and Octane servers,
authentication details, definition of which runs to fetch from ALM side.
All this data can be passed in a configuration file.

If the configuration file is named 'conf.xml' and is located in same
directory as this tool, it is automatically detected. Otherwise, pass the 
configuration file pathname as a command-line argument (-c option).

------------------------------------------------------------------------------------------------------------------------
Output *****************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
If an output option is specified (-o option), the tool writes the output XML to a file instead of pushing it to the Octane server.
If output file pathname parameter is missing, the output will be written into default file "output.xml"  in same  directory as this tool.
The tool saves only the first bulk (upto 1000 runs)
No Octane server or credential configuration is required in this case.


------------------------------------------------------------------------------------------------------------------------
Password handling ******************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
The password can be entered in the following ways:
*  Configuration file
*  Password is entered directly to command line (-pa and -po options)
*  Password is entered from file (-paf and -pof option). The file should contain only password as content.


------------------------------------------------------------------------------------------------------------------------
Run Filtering **********************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
The tool allow to filter runs from ALM, there are several predefined filters + option to define some of custom filters , for example
- fetch runs that has id equal or greater than X (startFromId filter)
- fetch runs that executed from some date (startFromDate filter)
- fetch runs from specific test type (testType filter)
- fetch runs that related to some release/sprint/test/testset (relatedEntity filter)
- fetch only first X runs (fetchLimit filter)


All filter options are optional and if specified more that one options, they are taken with "AND" operator .
The filters are located in the section of 'conf->alm->runFilter' section of Configuration
Some filter options are available also from command line
 - startFromId (option -rfid)
 - startFromDate (option -rfd)


As well, its possible to define some custom REST filter on run entity (see ALM help page about "ALM REST API" to find more how to filter in REST API)

------------------------------------------------------------------------------------------------------------------------
Log files **************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
The tool writew log information to 3 log files that are located in the "logs" directory
1.consoleLog.log : all information that is printed to console , also written to this file, so here you can find information about all your historical runs
2.restLog.log : all rest requests are persisted to this log
3.lastSent.txt : this file contains only last run id that was sent to Octane.

------------------------------------------------------------------------------------------------------------------------
Required permissions and Supported servers *****************************************************************************
------------------------------------------------------------------------------------------------------------------------

The tool supports 12.* versions of ALM.
The tool supports 12.53.19+ versions of Octane.

ALM user might have 'viewer' role and Octane user might have 'team member' role.
Octane user should be defined as API Access.


------------------------------------------------------------------------------------------------------------------------
Tool Usage Examples ****************************************************************************************************
------------------------------------------------------------------------------------------------------------------------

1. Configuration is loaded from default file ('conf.xml') that located in the same directory as this tool
    java -jar alm-test-result-collection-tool.jar

2. Configuration is loaded from file ('myNewConf.xml') that located in the same directory as this tool
    java -jar alm-test-result-collection-tool.jar -c myNewConf.xml

3. Tool save fetch results into default output file ('output.xml') that located in the same directory as this tool
    java -jar alm-test-result-collection-tool.jar -o

4. Tool fetch runs that starts from id 1000 by defining filter on command line
    java -jar alm-test-result-collection-tool.jar -rfid 1000

5. Tool get passwords from command line
    java -jar alm-test-result-collection-tool.jar -pa myAlmPassword -po myOctane password


------------------------------------------------------------------------------------------------------------------------
ALM 2 Octane Fields Mapping ********************************************************************************************
------------------------------------------------------------------------------------------------------------------------


--------------|--------------------|------------------------------------------------------------------------------------
Octane Entity | Octane Field       | Alm Field
--------------|--------------------|------------------------------------------------------------------------------------
Test          | Name               | If run's test name == run's test configuration name
              |                    |   Then => Format : AlmTestId #{testId} : {testName}
              |                    |   Else => Format : AlmTestId #{testId}, ConfId #{confId} : {testName} - {confName}
              | TestingToolType    | ALM Test type is converted to Octane TestingToolType as following :
              |                    |  "MANUAL=>Manual"; "LEANFT-TEST=>LeanFT"; "QUICKTEST_TEST=>UFT"; "BUSINESS-PROCESS=>BPT"
              |                    |   Other ALM test types are not converted.
              | Package*           | Project Name
              | Component*         | Domain Name
              | Class*             | Direct test folder name
--------------|--------------------|------------------------------------------------------------------------------------
Run           | Name               | Format : AlmTestSet #{testSetId} : {testSetName}
              | Duration           | Run Duration
              | ExternalReportUrl* | Td reference to run in the ALM Server (can be opened only in IE)
              | StartedTime        | Executed Date + Executed Time => transformed to Unix time
              | Status             | ALM run statuses 'Passed' and 'Failed' are taken as is, all other types are converted to "Skipped"
--------------|--------------------|------------------------------------------------------------------------------------

* The fields are visible only in grid view and not in document view

------------------------------------------------------------------------------------------------------------------------
Test Result API *******************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
The tool uses dedicated Test-Results API in order to send test results to Octane
(see more details in the URL http://<your-octane-server>:<port>/Help/WebUI/Online/Content/API/test-results.htm).


In first stage , the tool retrieve all relevant runs from ALM and send it to Octane to Test-Results API.
The API doesn't create tests and runs synchronously. Instead , the API returns a job ID which can later be queried for the test run creation status.
Later, the tool is querying the API about creation status for all created jobs.


The API create test and runs in Octane according to some uniquely identified fields of the entities
 - A test is uniquely identified by a combination of the following attributes: test name, component, package, and class.
 - A test run is uniquely identified by combination of the following: component, package, class, test name, and run name.

 According to the field mappings :
 - The test that was run with different test configurations - will be duplicated according to the number of test configurations (because test configuration is part of the test name)
 - The runs that were created in context of the same test and testset, they will be recognized as the same run, and in tab of TEST->Runs will appear only last executed run.
   Other runs will appear in tab "Previous Runs" of run details.
   If the run was created in context different testsets, Octane will show last run for each testset. To see name of testset - switch "runs" tab to grid view. Testset name is appear in name of run (see ALM 2 Octane Fields Mapping)


------------------------------------------------------------------------------------------------------------------------
Full configuration file example ****************************************************************************************
------------------------------------------------------------------------------------------------------------------------
        <?xml version="1.0" encoding="utf-8"?>
        <conf>
          <alm>

            <user></user>

            <password></password>

            <!--http://host:port/qcbin-->
            <serverUrl>http://myserver:8080/qcbin</serverUrl>

            <domain></domain>

            <project></project>

            <runFilter> <!--all parameters are optional, defined parameters are taken with AND operator -->
              <!--Fetch runs that has id greater than specified id. Possible value : any id or 'LAST_SENT', in last case - filtering is started from last sent run id (recognized automatically) -->
              <startFromId></startFromId>

              <!--Fetch runs that executed after specified date. Format yyyy-MM-dd-->
              <startFromDate></startFromDate>

              <!--Filter runs by test type, allowed test types : MANUAL, QUICKTEST_TEST,BUSINESS-PROCESS,LEANFT-TEST-->
              <testType></testType>

              <!--Filter runs that related to some entity, for example 'runs that related to release AAA' or runs frin  -->
              <relatedEntity>

                <!--Available types : test,testset,sprint, release-->
                <type></type>

                <!--id or ids separated with 'OR' : 1 OR 2 OR 3-->
                <id></id>
              </relatedEntity>

              <!--Any custom valid REST filter, example : assign-rcyc[1001 or 1002];id[>100 AND &lt;500]-->
              <custom></custom>

              <!--limit fetching runs from ALM, max is 200000 -->
              <fetchLimit></fetchLimit>

            </runFilter>

          </alm>

          <octane>
            <clientId></clientId>

            <clientSecret></clientSecret>

            <!--http://host:port-->
            <serverUrl>http://myserver:8080</serverUrl>

            <sharedSpaceId></sharedSpaceId>

            <workspaceId></workspaceId>

          </octane>

          <proxy>
            <host></host>
            <port></port>
          </proxy>
        </conf>


------------------------------------------------------------------------------------------------------------------------
Q & A ******************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
Q : How I can find in Octane tests that are fetched from ALM?
A : During fetching, we map ALM Domain name to test "component" field and ALM Project name to test "package" field.
    You can filter test with matching "component" and "package" to find fetched tests.

Q: On previous week, I fetched all existing runs from ALM to Octane. On this week, many new runs were added to ALM.
   I want to fetch To Octane only runs that were created after my last fetch.
A: You can filter runs that were created after your last fetch by defining runFilter option "startFromId" with value "LAST_SENT" in configuration file

Q: I have test that has been executed several times ,but in tab "Runs" , I see only last run. Where I can found other runs?
A: If all execution were done in context of the same testset in ALM,  Octane  will show only last run, other runs are appear in tab "Previous Runs" of run details.
   If there were execution in different testsets, Octane will show last run for each testset. To see name of testset -
   switch "runs" tab to grid view. Testset name is appear in name of run (see ALM 2 Octane Fields Mapping)

Q: How I can assign fetched tests to my Application Modules
A: Workspace Admin can define "Test Assignment Rules" for fetched tests. It can done in Configuration->Workspaces->Dev-Ops->Test Assignment Rules.
   The rules is created by using the following test fields :
   - ClassName (ALM direct test folder name)
   - Component ( ALM Domain Name)
   - Package (ALM Project Name)
   - Test Name




