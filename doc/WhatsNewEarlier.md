# What's new in earlier versions


This page shows a history of the enhancements made to the Micro Focus Application Automation Tools Jenkins plugin.

See the [Changelog ](https://wiki.jenkins.io/display/JENKINS/Changelog)for information about beta versions and recent patches.

[What&#39;s New in version 7.2](#what&#39;s-new-in-version-7&#46;2)

[What&#39;s New in version 7.1](#what&#39;s-new-in-version-7&#46;1)

[What&#39;s New in version 7.0](#what&#39;s-new-in-version-7&#46;0)

[What&#39;s New in version 6.9](#what&#39;s-new-in-version-6&#46;9)

[What&#39;s New in versions 6.7 and 6.8](#what&#39;s-new-in-versions-6&#46;7-and-6&#39;8)

[What&#39;s New in version 6.6](#what&#39;s-new-in-version-6.6)

[What&#39;s New in version 6.5](#what&#39;s-new-in-version-6.5)

[What&#39;s New in version 6.4](#what&#39;s-new-in-version-6.4)

[What&#39;s New in version 6.3](#what&#39;s-new-in-version-6.3)

[What&#39;s New in version 6.0](#what&#39;s-new-in-version-6.0)

[What&#39;s New in version 5.9](#what&#39;s-new-in-version-5.9)

## What&#39;s New in version 7&#46;2

Version 7.2 introduced the following enhancements:

**General**

- Updated plugin to handle log4j security issue.

**UFT One**

- Parsing of UFT test names by Jenkins.

## What&#39;s New in version 7&#46;1

Version 7.1 introduced the following enhancements:

**General**

- More user-friendly layout

**LoadRunner Entrerprise**

- Security fix: Beginning with Jenkins version 2.303.3, the LoadRunner Enterprise build step requires the security fix introduced in this release

**ALM Octane**

- Support execution of BDD tests in testing framework
- UFT One test runner can inject Octane details as test parameters. (This requires you to define the global parameter ADD_GLOBAL_PARAMETERS_TO_TESTS.)
- Support for the Fortify SSC plugin version 21.1.36 
- Supports triggering test suites from Jenkins, from the Execute tests build step
 
**UFT One**

- Parsing test parameters enhancement for UFT One Tests through Jenkins, for file system executions. For limitations, see [Limitations in Test Parameters syntax](#test-parameters-syntax).
- The **Warning** test status is now handled for tests executed from ALM
- Final job status improvements for jobs exceeding the specified timeout
- More secure credential handling with the introduction of Job level and Global level credential handling
- Defect fix: Handling of special characters and the NULL value for test parameters
- Defect fix: Non-existent test sets are now handled and taken into consideration in the job execution

## What&#39;s New in version 7&#46;0

Version 7.0 introduced the following enhancements:

**ALM Octane**

- Support for pipeline stop
- Support for BDD scenarios in test runners
- Support for version 21.1.36 of the Fortify SSC plugin  
- Defect fix - Octane builds no longer point to obsolete UFTOne run results

**UFT One**

- Ability to use the Micro Focus plugin on an air-gap network (not connected to the Internet) for ALM executions
- Run Results enhancements, including the use of MTBX files for test execution
- Defect fix - Layout issues were handled

**LoadRunner Enterprise**

- Defect fix - It is now possible to browse for the Test ID in the Jenkins plugin.

## What&#39;s New in version 6&#46;9

Version 6.9 introduced the following enhancements:

**ALM Octane**

- Enabled branch publisher
- Support added for Java 11

**Workarounds**

- For running UFTM tests from UFT One: If UFTM tests fail in UFT One after changing the UFTM server version in the Jenkins configuration, manually restart UFT One and run the test again.

## What&#39;s New in versions 6.7 and 6.8

Versions 6.7 and 6.8 introduced the following enhancements:

**General**

- Security fixes: Security issues noted in the *Jenkins Security Advisory 2021-04-07* were addressed. For details, search for SECURITY-2132, SECURITY-2175, and SECURITY-2176 in the [Jenkins Security Advisory 2021-04-07 page](https://www.jenkins.io/security/advisory/2021-04-07/). (Version 6.8)
- Added support for the FOD plugin version 6.1.0.
- SonarQube integration: A new option was added to the Sonar pre-build step to skip automatic webhook configuration. (This allows you to run the integration without admin permissions.)
 - Added XSRF protection for running with Lab, Upload result, and Common result upload for compatibility with new versions of ALM.

**ALM Octane – UFT Integration**

- Reduced the length of the folder name for checkout of auto-generated execution jobs.
- [Bug fix] Ability to execute UFT tests not located in job workspace. To show the test name correctly in ALM Octane, define the "UFT_CHECKOUT_FOLDER" parameter.
- [Bug fix] For the Spanish Windows slave: The duration of a UFT test in Jenkins now displays correctly, in seconds.

**ALM Octane**

- Added the option to inject pull requests to ALM Octane with repository in SSH format.
- Auto-fill: After injection of pull-requests, templates related to SCM repositories are auto-filled in ALM Octane (Settings > Spaces-> DevOps > SCM Repositories).

**LRE (LoadRunner Enterprise)**

- The build step “Execute performance test using LoadRunner Enterprise” can authenticate to LoadRunner Enterprise using an access key (available from LoadRunner Enterprise 2021 R1).

## What&#39;s New in version 6.6

Version 6.6 introduced the following enhancements:

**General**

- Compliance with the new Jenkins version 2.264 form layout
- Certified for Jenkins version 2.264.1

## What&#39;s New in version 6.5

Version 6.5 introduced the following enhancements:

**General**

- “github-branch-source” dependency was removed.
- Certified for Jenkins version 2.263.1.

**ALM Octane**

- New option to cache the job list for pipeline creation in ALM Octane
- Caching of Octane pipeline roots, to avoid sending non-relevant events/tasks/logs to ALM Octane
- Added ability to execute UFT tests with parameters from ALM Octane
- Bug fix: UFT test descriptions are properly formatted in ALM Octane

## What&#39;s New in version 6.4

Version 6.4 introduced the following enhancements:

**General**

- Multiple dependencies updated. You can verify your Jenkins plugins compatibility with the [plugin dependencies](https://plugins.jenkins.io/hp-application-automation-tools-plugin/#dependencies).
- Certified for Jenkins version 2.249.1.

**ALM Octane**

- Ability to configure folder for UFT Test Runner creation
- Ability to create auto-generated UFT jobs with SSH credentials
- Reduced name length of UFT auto-generated Test Runner jobs
- Added support for Configuration-as-code plugin to ALM Octane configuration
- collectPullRequestsToAlmOctane pipeline step – now supports environment parameters for all fields (including credentialsId)

**UFT One**

- Migration of ALM credentials from the Task configuration page to global configuration

## What&#39;s New in version 6.3

Version 6.3 introduced the following enhancements:

**ALM Octane**

- Ability to disable configuration

- Ability to configure jenkinsUser per ALM Octane workspace and to restrict job visibility during new pipeline creation

- Ability to configure ALM Octane allowed storage for logs

- Support for base64 encoding for jobIds (relevant for ALM Octane behind an Apache server)

- New icons indicating the connected workspaces

- Bug fix: The UFT Discovery job now populates the description of UFT API tests.

- Bug fix: Connectivity issue where ALM indicated that Jenkins was not connected, was resolved.

- Bug fix: CI pipeline jobs are automatically updated when you rename or move jobs.

**UFT One**

- Support for defining the number of iterations for UFT tests

## What&#39;s New in version 6.0

Version 6.0 introduced the following enhancements:

**ALM Octane**

- You can now SET/GET milestones on ALM Octane Pipeline configurations.


## What&#39;s New in version 5.9

Version 5.9 introduced the following enhancements:

**ALM Octane**

- Support for custom converters to test frameworks

- Improvements in logs

- Support for additional versions of the Sonarqube scanner plugin (2.6.1, 2.8.1, 2.9)

- Bug fixes

**UFT Mobile (Mobile Center)**

- Rebrand from &quot;Mobile Center&quot; to &quot;UFT Mobile&quot;

**ALM**

- Improvements in the logic of parsing test case status.

https://issues.jenkins-ci.org/browse/JENKINS-58134 

**UFT**

- Support for ALM 15.0 with SSO 

- Refactoring of HpToolsLauncher

- Bug fixes



