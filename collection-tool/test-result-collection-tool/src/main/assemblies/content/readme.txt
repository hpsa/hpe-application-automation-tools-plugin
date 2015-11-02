README
======

HP Lifecycle Management Test Result Collection Tool
---------------------------------------------------

Test Result Collection Tool is command line tool for pushing test result 
XML file(s) to MQM test result public API.

Usage
-----

 java -jar test-result-collection-tool.jar [OPTIONS]... FILE [FILE]...

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
 -i,--internal                     supplied xml files are in the internal
                                   xml format
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
 -s,--server <URL:PORT>            server url with protocol and port
    --started <TIMESTAMP>          started time in millis
 -t,--tag <TYPE:VALUE>             assign tag to test result
 -u,--user <USERNAME>              server username
 -v,--version                      show version of this tool
 -w,--workspace <ID>               server workspace to push to

Configuration
-------------

In order to push test results to MQM server, this tool requires 
specification of the server location (-s option), sharedspace ID (-d 
option) and workspace ID (-w option), which can be
specified directly as a command-line arguments or in a configuration file 
as shown on the following example:

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

Location of this configuration file can be manually specified as an 
argument (-c option) or it can be automatically detected (when file with 
configuration named 'config.properties'
is placed in the same directory as this tool).

Taxonomy tags, field tags, product areas and backlog items can be 
specified multiple times (e.g. -t "OS:Linux" -t "DB:Oracle"). If there is 
an output file specified (--output-file option), this tool will write the 
output XML (created from a single input JUnit report) to file instead of 
pushing it to the server. No server or credential specification is 
required in this case. If there is no command line specification of the 
started time (--started option), current system time will be used in this 
field for non-internal test results. Some server-side errors (e.g. invalid 
release ID) can cause test result push failure even when the pushed XML is 
well formatted. User can use a skip-errors flag (-e option) to push such a 
test result anyway.

Password handling
-----------------

There are three ways, how to enter the password:

1.  User is prompted to enter password to console

2.  Password is entered directly to command line (--password option)

3.  Password is entered from file (--password-file option)

Supported test result formats
-----------------------------

This tool accepts JUnit test reports. This format is shown on the 
following example:

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

Additional information like release, taxonomy tags or field tags can be 
set as command line arguments for JUnit test reports.

User can also provide the test report in public API format for more 
complex use cases. All additional parameters (release, taxonomy tags, 
field tags,...) are set directly in the XML file in this case.

Examples
--------

1.  Server configuration is entered directly on the command line. User 
    will be prompted to enter the password.

    java -jar test-result-collection-tool.jar -s "http://localhost:8080" 
        -d 1001 -w 1002 JUnit.xml

2.  Configuration of the server is specified in a separate configuration 
    file. Password is entered directly on the command line and tags will be
    assigned to the test result generated from both JUnit files.

    java -jar test-result-collection-tool.jar -c someConfig.properties -p 
        "password" -t "OS:Linux" -t "DB:Oracle" JUnitOne.xml JUnitTwo.xml

3.  Server configuration is automatically loaded from the 
    'config.properties' file, which is placed in the same directory as this
    tool. The input file is in a public API format.

    java -jar test-result-collection-tool.jar -i publicApi.xml
