README
======

HPE ALM Test Result Collection Tool
------------------------------------------------------------------------------------------------------------------------

The HPE ALM Test Result Collection Tool is a command line tool for retrieving test results from ALM and sending them to ALM Octane.

------------------------------------------------------------------------------------------------------------------------
Usage ******************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------

java -jar alm-test-result-collection-tool.jar [OPTIONS]...


 -c,--config-file <FILE>               Configuration file location. Default configuration file name is 'conf.xml'
 -h,--help                             Show this help
 -o,--output-file <FILE>                     Write output to file instead of sending it to ALM Octane. File path is optional. 
                                       Default file name is 'output.xml'.
                                       When saving to a file, the tool saves up to 1000 runs. 
                                       No ALM Octane URL or authentication configuration is required if you use this option.
 -pa,--password-alm <PASSWORD>         Password for ALM user to use for retrieving test results
 -paf,--password-alm-file <FILE>       Location of file with password for ALM user
 -po,--password-oct <PASSWORD>         Password for ALM Octane user
 -pof,--password-oct-file <FILE>       Location of file with password for ALM Octane user
 -rfd,--run-filter-date <YYYY-MM-DD>   Filter the ALM test results to retrieve only test runs from this date or later
 -rfid,--run-filter-id <ID>            Filter the ALM test results to retrieve only test runs with this run ID or higher
 -rfl,--run-filter-limit <NUMBER>      Limit number of ALM runs to retrieve 
 -v,--version                          Show version of this tool

------------------------------------------------------------------------------------------------------------------------
Configuration **********************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
To retrieve test results from ALM and send them to ALM Octane, this tool requires ALM and ALM Octane URLs,
authentication details, definition of which runs to retrieve from ALM side.
All this data can be passed in a configuration file. For details about the configuration file format and structure, see the end of this readme.

If the configuration file is named 'conf.xml' and is located in the same
directory as this tool, it is automatically detected. Otherwise, pass the 
configuration file pathname as a command-line argument (-c option).


------------------------------------------------------------------------------------------------------------------------
Password handling ******************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
The password can be entered in the following ways:
*  Configuration file
*  Password is entered directly to command line (-pa and -po options)
*  Password is entered from file (-paf and -pof option). The file should contain only the password.


------------------------------------------------------------------------------------------------------------------------
Run Filtering **********************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
You can specify filters that limit the test results retrieved from ALM. 
Specify the filter using command line options or elements in the 'conf->alm->runFilter' section of the configuration file.
For details about the configuration file format and structure, see the end of this readme.

Use predefined filter options, or  design a REST-based custom filter.  
Predefined filters:
- Retrieve test results from runs whose ID is equal to or greater than X (startFromId element in config file, -rfid option in command line)
- Retrieve test results from runs that ran on a  specified date or later (startFromDate element in config file, -rfd option in command line)
- Retrieve runs  of a specific test type (testType element in config file)
- Retrieve runs related to some release/sprint/test/test set (relatedEntity element in config file)
- Retrieve only the first X runs (retrievalLimit element in config file, -rfl option in command line)

All filter options are optional. If you specify more than one option, they are used  with "AND" logic.

REST-based custom filter:
In the 'conf->alm->runFilter' section of the configuration file, define a <custom> element with a  REST filter on the
run entity (for details about building a REST filter, see the ALM help page about "ALM REST API").

------------------------------------------------------------------------------------------------------------------------
Log files **************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
The tool writes log information to 3 log files located in the "logs" directory.
1. consoleLog.log : all information printed to the console is also written to this file. This is where you can find historical information about past runs.
2. restLog.log : all REST requests are stored in this log
3. lastSent.txt : this file contains only the ID of the last run that was sent to ALM Octane.

------------------------------------------------------------------------------------------------------------------------
Required permissions and Supported servers *****************************************************************************
------------------------------------------------------------------------------------------------------------------------

The tool supports 12.* versions of ALM.
The tool supports 12.53.19+ versions of ALM Octane.

The ALM and ALM Octane users used by the tool can have the lowest permission levels: 'viewer' role in ALM, and 'team member' role in ALM Octane.
In ALM Octane, you must define API Access keys for the tool to use.

------------------------------------------------------------------------------------------------------------------------
Tool Usage Examples ****************************************************************************************************
------------------------------------------------------------------------------------------------------------------------

1. Run the ALM Test Result Collection tool using the default configuration file location ('conf.xml' in the same directory as the tool):
    java -jar alm-test-result-collection-tool.jar

2. Run the ALM Test Result Collection tool using the configuration file 'myNewConf.xml' located in the same directory as the tool:
    java -jar alm-test-result-collection-tool.jar -c myNewConf.xml

3. Run the ALM Test Result Collection tool, saving retrieved results in the default output file ('output.xml' located in the same directory as the tool):
    java -jar alm-test-result-collection-tool.jar -o

4. Run the ALM Test Result Collection tool, retrieving results form runs whose ID is 1000 or more. The filter is  defined using a command line option.
    java -jar alm-test-result-collection-tool.jar -rfid 1000

5. Run the ALM Test Result Collection tool. Provide passwords in the command line
    java -jar alm-test-result-collection-tool.jar -pa myAlmPassword -po myOctanepassword


------------------------------------------------------------------------------------------------------------------------
ALM 2 ALM Octane Field Mapping *****************************************************************************************
------------------------------------------------------------------------------------------------------------------------


------------------|--------------------|--------------------------------------------------------------------------------
ALM Octane Entity | ALM Octane Field       | ALM Field
------------------|--------------------|--------------------------------------------------------------------------------
Test              | Name               | If run's test name == run's test configuration name
                  |                    |   Then => Format : AlmTestId #{testId} : {testName}
                  |                    |   Else => Format : AlmTestId #{testId}, ConfId #{confId} : {testName} - {confName}
                  | TestingToolType    | ALM Test type is converted to ALM Octane TestingToolType as follows :
                  |                    |  "MANUAL=>Manual"; "LEANFT-TEST=>LeanFT"; "QUICKTEST_TEST=>UFT"; "BUSINESS-PROCESS=>BPT"
                  |                    |   Other ALM test types are not converted.
                  | Package*           | Project Name
                  | Component*         | Domain Name
                  | Class*             | Direct test folder name
------------------|--------------------|--------------------------------------------------------------------------------
Run               | Name               | Format : AlmTestSet #{testSetId} : {testSetName}
                  | Duration           | Run Duration
                  | ExternalReportUrl* | Td reference to run in the ALM Server (can be opened only in IE)
                  | StartedTime        | Executed Date + Executed Time => transformed to Unix time
                  | Status             | ALM run statuses 'Passed' and 'Failed' are taken as is, all other types are converted to "Skipped"
------------------|--------------------|---------------------------------------------------------------------------------

* In ALM Octane, these fields are visible only in the Tests/Runs grid and not when you open a specific automated test or run. 


------------------------------------------------------------------------------------------------------------------------
Test Result API ********************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
This tool uses a dedicated Test-Results API in order to send test results to ALM Octane
(see more details in the URL http://octane-help.saas.hpe.com/en/Latest/Online/Help_Center.htm#cshid=test_results_api).

First, the tool retrieves all relevant test results from ALM and sends them to ALM Octane to the test-results API. The test results are sent in bulks of 1000 at a time.
Next, the API creates the matching automated tests and test runs in ALM Octane. If a relevant automated test already exists, the results are associated with that test.
The API does not create tests and runs synchronously. Therefore, the API returns a job ID that the tool uses later to query the API about the test run creation status.


The API creates test and runs in ALM Octane based on  uniquely identifiable fields of the entities:
 - A test is uniquely identified by a combination of the following attributes: component, package, class, and test name.
 - A test run is uniquely identified by a combination of the following       : component, package, class, test name, and run name.

 According to the field mapping described above:
 - Tests that ran with different test configurations will be duplicated according to the number of test configurations (because test configuration is part of the test name).
 - Runs created in context of the same test and test set, are recognized as consecutive runs. The details of the last run are saved in the Details of the ALM Octane test run entity.
   All other runs are saved as previous runs of the same entity.
   If the results were created by runs in different test sets, ALM Octane will have an automated run entity for the last run for each test set.
   A run’s test set name is part of the run name (see ALM 2 ALM Octane Fields Mapping).


------------------------------------------------------------------------------------------------------------------------
Full configuration file example ****************************************************************************************
------------------------------------------------------------------------------------------------------------------------
        <?xml version="1.0" encoding="utf-8"?>
        <conf>
          <alm>
            <user></user>

            <password></password>

            <!--http://host:port/qcbin-->
            <serverUrl></serverUrl>

            <domain></domain>

            <project></project>

            <runFilter> <!--all parameters are optional, defined parameters are used with AND logic -->
              <!--Retrieve runs with IDs equal to or greater than the specified ID. Possible values : any ID or 'LAST_SENT'. For LAST_SENT, filtering is started the run that followed the last sent run ID -->
              <startFromId></startFromId>

              <!--Retrieve runs executed on or after a specified date. Format yyyy-MM-dd-->
              <startFromDate></startFromDate>

              <!--Filter runs by test type. Possible values: MANUAL, QUICKTEST_TEST, BUSINESS-PROCESS, LEANFT-TEST. 
			  You can select one or several types separated with 'OR' : MANUAL OR QUICKTEST_TEST-->
              <testType></testType>

              <!--Retrieve runs related to a specific entity, for example 'runs that related to release AAA' or ‘runs from sprint 5’. Provide the entity type and ID-->
              <relatedEntity>

                <!--Supported types : test, testset, sprint, release-->
                <type></type>

                <!--ID or IDs separated with 'OR' : 1 OR 2 OR 3-->
                <id></id>
              </relatedEntity>

              <!--Any custom valid REST filter, example : assign-rcyc[1001 or 1002];id[>100 AND &lt;500]-->
              <custom></custom>

              <!--Limit number of runs retrieved from ALM, max is 200000 -->
              <retrievalLimit></retrievalLimit>

            </runFilter>

          </alm>

          <alm-octane>
            <clientId></clientId>

            <clientSecret></clientSecret>

            <!--http://host:port-->
            <serverUrl></serverUrl>

            <sharedSpaceId></sharedSpaceId>

            <workspaceId></workspaceId>

          </alm-octane>

          <proxy>
            <!--proxy hostname or IP address (without http://)-->
            <host></host>
            <!-- proxy port number-->
            <port></port>
          </proxy>
        </conf>


------------------------------------------------------------------------------------------------------------------------
Q & A ******************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
Q : In ALM Octane, how I can find  the tests that were retrieved from ALM?
A : During retrieval, the ALM Domain name is entered in the test's "component" fields, and the ALM Project name is entered in the 
    test's "package" fields. In ALM Octane, you can locate the tests by filtering for the relevant  "component" and "package" fields.

Q: Last week,  I retrieved all existing test results from ALM to ALM Octane. This week, many new runs were added to ALM. I want to 
   retrieve to ALM Octane only runs that were created after my last retrieval.
A: You can filter runs that were created after your last retrieval by defining the runFilter option "startFromId" in configuration 
   file with the value "LAST_SENT".

Q: I have a test that was executed several times, but in the "Runs" tab, I see only last run. Where can I find the other runs?
A: If all test runs were in the context of the same test set in ALM, ALM Octane sees all runs as repeats of the same one, and  
   maintains only last run. Other runs are displayed in the run’s "Previous Runs" tab .
   If the test runs  were  in different test sets, ALM Octane has an automated run entity for the last run  for each test set. The test set 
   name is part of the run name (see ALM 2 ALM Octane Fields Mapping). The name is not displayed in the smart list view.

Q: How I can assign  tests retrieved from ALM to my ALM Octane application modules?
A: As a workspace admin, define Test Assignment Rules for the automated tests retrieved from ALM. 
   Open Configuration->Workspaces->Dev-Ops->Test Assignment Rules. Create rules  using the following test fields :
   - ClassName (ALM direct test folder name)
   - Component ( ALM Domain Name)
   - Package (ALM Project Name)
   - Test Name
  For more detail on creating test assignment rules, see the ALM Octane Help.
