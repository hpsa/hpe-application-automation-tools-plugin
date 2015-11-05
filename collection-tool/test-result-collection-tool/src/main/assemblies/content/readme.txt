README
======

HP Lifecycle Management Test Result Collection Tool
---------------------------------------------------

The Test Result Collection Tool is a command line tool for pushing test 
result XML files to the MQM test result API.

Usage
-----

 java -jar test-result-collection-tool.jar [OPTIONS]... FILE [FILE]...
 
 OPTIONS:
 -a,--product-area <ID>            assign the test result to product area
 -b,--backlog-item <ID>            assign the test result to backlog item
 -c,--config-file <FILE>           configuration file location
    --check-result                 check test result status after push
    --check-result-timeout <SEC>   timeout for test result push status
                                   retrieval
 -d,--shared-space <ID>            server shared space to push to
 -e,--skip-errors                  skip errors on the server side
 -f,--field <TYPE:VALUE>           assign field tag to test result
 -h,--help                         show this help
 -i,--internal                     supplied XML files are in the API
                                   internal XML format
 -o,--output-file <FILE>           write output to file instead of pushing
                                   it to the server
 -p,--password <PASSWORD>          server password
    --password-file <FILE>         location of file with server password
    --proxy-host <HOSTNAME>        proxy host
    --proxy-password <PASSWORD>    proxy password
    --proxy-password-file <FILE>   location of file with proxy password
    --proxy-port <PORT>            proxy port
    --proxy-user <USERNAME>        proxy username
 -r,--release <ID>                 assign release to test result
 -s,--server <URL:PORT>            server URL with protocol and port
    --started <TIMESTAMP>          start time in milliseconds
 -t,--tag <TYPE:VALUE>             assign tag to test result
 -u,--user <USERNAME>              server username
 -v,--version                      show version of this tool
 -w,--workspace <ID>               server workspace to push to

Configuration
-------------

To push test results to MQM server, this tool requires the server location 
(-s option), sharedspace ID (-d option) and workspace ID (-w option). 
This data can be passed as command-line arguments or in a configuration file. 

Example configuration file:

    # Server URL with protocol and port
    server=http://myserver.hpe.com:8080
    # Server sharedspace ID
    sharedspace=1001
    # Server workspace ID
    workspace=1002
    # Server username
    user=test@hpe.com
    # Proxy host address
    proxyhost=proxy.hpe.com
    # Proxy port number
    proxyport=8080
    # Proxy username
    proxyuser=test

If the configuration file is named 'config.properties' and is in same 
directory as this tool, it is automatically detected. Otherwise, pass the 
configuration file pathname as a command-line argument (-c option). 

Taxonomy tags, field tags, product areas and backlog items can be specified 
more than once (e.g. -t "OS:Linux" -t "DB:Oracle"). 

If an output file is specified (--output-file option), this tool writes 
the output XML to a file instead of pushing it to the server. No server or 
credential specification is required in this case.
The output XML is created from a single input JUnit report.

If there is no command line specification of the start time (--started 
option), the current system time is used for JUnit test results. 

Some server-side errors can cause test result push failure even when the 
pushed XML is well formatted. You can use the skip-errors flag (-e option) to 
force pushing such a test result.

Password handling
-----------------

The password can be entered in the following ways:
*  User is prompted to enter password on console
*  Password is entered directly to command line (--password option)
*  Password is entered from file (--password-file option)

Supported test result formats
-----------------------------

This tool accepts JUnit test reports. This format is shown in the following example:

    <!-- element encapsulating testcases -->
    <testsuite>
        <!-- testcase contains mandatory attribute 'name' -->
        <!-- and optionally 'classname', 'time' -->
        <testcase classname="com.examples.example.SampleClass" 
            name="passedTest" time="0.001"/>
        <!-- 'skipped' element is present for skipped tests -->
        <testcase name="skippedTest" time="0.002">
            <skipped/>
        </testcase>
        <!-- 'failure' element is present for failed tests -->
        <testcase name="failedTest">
            <failure/>
        </testcase>
        <!-- 'error' element is present for tests with error -->
        <testcase name="testWithError" time="0.004">
            <error/>
        </testcase>
    </testsuite>

Additional information like release, taxonomy tags, or field tags can 
be set as command line arguments for JUnit test reports.

You can also provide the test report in the API internal format by using 
the -i or --internal option. If you use the API internal format, more parameters are 
set in the XML file than are available in the JUnit test reports. For  
example, release, taxonomy tags, field tags,and so on. The API internal  
format is defined in testResult.xsd, on the MQM server.

Examples
--------

1.  Server configuration is entered directly on the command line. 
User is prompted to enter the password.

    java -jar test-result-collection-tool.jar -s "http://localhost:8080" 
        -d 1001 -w 1002 JUnit.xml

2.  Configuration of the server is specified in a separate configuration  
file. Password is entered directly on the command line and tags are assigned to 
the test results generated from both JUnit files.

    java -jar test-result-collection-tool.jar -c someConfig.properties -p 
        "password" -t "OS:Linux" -t "DB:Oracle" JUnitOne.xml JUnitTwo.xml

3.  Server configuration is automatically loaded from the 'config.properties' 
file, which is placed in the same directory as this tool. See the format
example above in the "Configuration" section.

    java -jar test-result-collection-tool.jar -i publicApi.xml
