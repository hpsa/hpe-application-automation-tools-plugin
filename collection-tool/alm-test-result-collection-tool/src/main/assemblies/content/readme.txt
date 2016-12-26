README
======

HPE ALM Test Result Collection Tool
------------------------------------------------------------------------------------------------------------------------

The HPE ALM Test Result Collection Tool is a command line tool for fetching test result from ALM into Octane server.

------------------------------------------------------------------------------------------------------------------------
Usage ******************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------

java -jar alm-test-result-fetcher.jar [OPTIONS]...


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
Tool Flow **************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
The activity of the tool can be divided to 3 parts
1.Fetch data from ALM : the tool fetch runs and its related entity from ALM by using REST API. Each request -
 fetches upto 200 item. Note : during fetching , console print '.' for each request to indicate progress in fetching of entities.
2.Send data to Octane : The data is sent in the XML format through dedicate TestResuls API.
(see more details ~your-octane-server:port/Help/WebUI/Online/Content/API/test-results.htm). On each post - 1000 runs are sent.
 Each post - return "Sent Id" for tracking about the status of the bulk.
3.Persistence validation in Octane : in this stage we validate that all posts are persisted successfully.
  The tracking is done by using "Sent Id" from previous step.


------------------------------------------------------------------------------------------------------------------------
Output *****************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
Instead of sending data to Octane, its possible to save data that should be sent to Octane to some file.
If an output option is specified (-o option), this tool writes
the output XML to a file instead of pushing it to the server.
If output file pathname parameter is missing, the output will be written into default file
"output.xml"  in same  directory as this tool.
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


As well, its possible to define some custom REST filter on run entity (see help page about "ALM REST API" to find more how to filter in REST API)

------------------------------------------------------------------------------------------------------------------------
Log files **************************************************************************************************************
------------------------------------------------------------------------------------------------------------------------
The tool is write log information to 3 log files that are located in the "logs" directory
1.consoleLog.log : all information that is printed to console , also written to this file, so here you can find information about all your historical runs
2.restLog.log : all rest request are persisted to this log
3.lastSent.txt : this file contains only id of the last run id that was sent to Octane. So, tool would be able to recognize

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
    java -jar alm-test-result-fetcher.jar

2. Configuration is loaded from file ('myNewConf.xml') that located in the same directory as this tool
    java -jar alm-test-result-fetcher.jar -c myNewConf.xml

3. Tool save fetch results into default output file ('output.xml') that located in the same directory as this tool
    java -jar alm-test-result-fetcher.jar -o

4. Tool fetch runs that starts from id 1000 by defining filter on command line
    java -jar alm-test-result-fetcher.jar -rfid 1000

4. Tool get passwords from command line
    java -jar alm-test-result-fetcher.jar -pa myAlmPassword -po myOctane password


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
              | Package            | Project Name
              | Module             | Domain Name
              | Class              | Direct test folder name
--------------|--------------------|------------------------------------------------------------------------------------
Run           | Name               | Format : AlmTestSet #{testSetId} : {testSetName}
              | Duration           | Run Duration
              | ExternalReportUrl  | Td reference to run in the ALM Server (can be opened only in IE)
              | StartedTime        | Executed Date + Executed Time => transfomed to Unix time
              | Status             | ALM run statuses 'Passed' and 'Failed' are taken as is, all other types are converted to "Skipped"
--------------|--------------------|------------------------------------------------------------------------------------




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
              <startFromDate>2016-01-01</startFromDate>

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
A : During fetching, we map ALM Domain name to test "module" field and ALM Project name to test "package" field.
    You can filter test with matching "module" and "package" to find fetched tests.

Q: On previous week, I fetched all existing runs from ALM to Octane. On this week, many new runs were added to ALM.
   I want to fetch To Octane only runs that were created after my last fetch.
A: You can filter runs that were created after your last fetch by defining runFilter option "startFromId" with value "LAST_SENT" in configuration file

