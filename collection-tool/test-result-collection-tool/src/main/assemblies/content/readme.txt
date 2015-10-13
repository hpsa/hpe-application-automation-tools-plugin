README
======

HP Lifecycle Management Test Result Collection Tool
---------------------------------------------------

Test Result Collection Tool is command line tool for pushing test result
XML file(s) to MQM test result public API.

Usage
-----

 java -jar test-result-collection-tool.jar [OPTIONS]... FILE [FILE]...

 -a,--product-area <ID>      product area
 -c,--config-file <FILE>     configuration file
 -d,--shared-space <ID>      shared space
 -e,--skip-errors            skip errors on the server side
 -f,--field <TYPE:VALUE>     field tag
 -h,--help                   show this help
 -i,--internal               internal test result public API xml format
 -o,--output-file <FILE>     output to file
 -p,--password <PASSWORD>    password
    --password-file <FILE>   file with password
 -q,--requirement <ID>       requirement
 -r,--release <ID>           release
 -s,--server <URL>           server
    --started <TIMESTAMP>    started time in millis
 -t,--tag <TYPE:VALUE>       tag
 -u,--user <USERNAME>        username
 -v,--version                show version
 -w,--workspace <ID>         workspace

Configuration
-------------

In order to push test results to MQM server, this tool requires
specification of the server location (-s option),
sharedspace ID (-d option) and workspace ID (-w option), which can be
specified directly as a command-line arguments or in a configuration
file, which allows to specify four properties - server, sharedspace,
workspace and user. Location of this configuration file can be manually
specified as an argument (-c option) or it can be automatically
detected (when file with configuration named 'config.properties'
is placed in the same directory as this tool).

Both taxonomy tags and field tags can be specified multiple
times (e.g. -t "OS:Linux" -t "DB:Oracle"). If there is an output file
specified (--output-file option), this tool will write the output XML
to file instead of pushing it to the server. No server or credential
specification is required in this case. If there is no command line
specification of the started time (--started option), current system
time will be used in this field for test results. Some server-side
errors (e.g. invalid release ID) can cause test result push failure
even when the pushed XML is well formatted. User can use a skip-errors
flag (-e option) to push such a test result anyway.

Password handling
-----------------

There are three ways, how to enter the password:

1.  User is prompted to enter password to console

2.  Password is entered directly to command line (--password option)

3.  Password is entered from file (--password-file option)


Supported test result formats
-----------------------------

This tool accepts standard Surefire test report (produced
by JUnit/TestNG/...). Additional information like release,
taxonomy tags or field tags can be set as command line arguments.

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
    file. Password is entered directly on the command line and tags
    will be assigned to the test result generated from both JUnit files.

    java -jar test-result-collection-tool.jar -c someConfig.properties
        -p "password" -t "OS:Linux" JUnitOne.xml JUnitTwo.xml

3.  Server configuration is automatically loaded from the
    'config.properties' file, which is placed in the same directory as
    this tool. The input file is in a public API format.

    java -jar test-result-collection-tool.jar -i publicApi.xml
