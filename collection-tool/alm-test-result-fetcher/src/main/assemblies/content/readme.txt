README
======

HP Lifecycle Management Test Result Collection Tool
---------------------------------------------------

The Test Result Collection Tool is a command line tool for fetching test result from ALM into Octane server.

Usage:
-------

java -jar alm-test-result-fetcher.jar [OPTIONS]...


 -c,--config-file <FILE>               configuration file location. Default configuration file name is 'conf.xml'
 -h,--help                             show this help
 -o,--output-file                      write output to file instead of pushing it to the Octane server. File path is optional. Default file name is 'output.xml'
 -pa,--password-alm <PASSWORD>         password for alm user
 -paf,--password-alm-file <FILE>       location of file with password for alm user
 -po,--password-oct <PASSWORD>         password for octane user
 -pof,--password-oct-file <FILE>       location of file with password for octane user
 -rfd,--run-filter-date <YYYY-MM-DD>   start run fetching from date
 -rfid,--run-filter-id <ID>            start run fetching from id
 -v,--version                          show version of this tool

Configuration
-------------

To fetch test results from ALM to Octane server, this tool requires the server location of ALM and Octane servers
This data can be passed in a configuration file.

If the configuration file is named 'conf.xml' and is in same
directory as this tool, it is automatically detected. Otherwise, pass the 
configuration file pathname as a command-line argument (-c option). 

If an output file is specified (-o option), this tool writes
the output XML to a file instead of pushing it to the server.
If file pathname parameter is missing, the output will be written into default file
"output.xml"  in same  directory as this tool.
No Octane server or credential configuration is required in this case.


Password handling
-----------------

The password can be entered in the following ways:
*  Configuration file
*  Password is entered directly to command line (-pa and -po options)
*  Password is entered from file (-paf and -pof option)


Run Filtering
--------------
The tool allow to filter runs from ALM, there are several predefined filters + option to define some of custom filters.
All filter are translated to ALM Rest API filter.
Here are extraction of runFilter section from configuration file

         <runFilter> <!--all parameters are optional, defined parameters are taken with AND operator-->
              <!--Fetch runs that has id greater than specified id. Possible value : any id or 'LAST_SENT', in last case - filtering is started from last sent run id (recognized automatically) -->
              <startFromId></startFromId>

              <!--Fetch runs that executed after specified date. Format yyyy-MM-dd-->
              <startFromDate>2016-01-01</startFromDate>

              <!--Filter runs by test type, allowed test types : QUICKTEST_TEST,BUSINESS-PROCESS,LEANFT-TEST-->
              <testType></testType>

              <!--Filter runs that related to some entity, for example 'runs that related to release AAA' or runs frin  -->
              <relatedEntity>

                <!--Available types : test,testset,sprint, release-->
                <type></type>

                <!--id or ids separated with 'OR' : 1 OR 2 OR 3-->
                <id></id>
              </relatedEntity>

              <!--Any custom valid filter, example : assign-rcyc[1001 or 1002];id[>100 AND &lt;500]-->
              <custom></custom>

              <!--limit fetching runs from ALM, max is 200000 -->
              <fetchLimit></fetchLimit>
          </runFilter>

Some filter options are available also from command line
 - startFromId (option -rfid)
 - startFromDate (option -rfd)

Examples
--------

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


Required permissions and Supported servers
----------------------------------------------

The tool supports 12.* versions of ALM.
The tool supports 12.53.19+ versions of Octane.
ALM user might have 'viewer' role and Octane user might have 'team member' role.
Octane user should be defined as API Access

