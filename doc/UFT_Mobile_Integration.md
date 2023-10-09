# Digital Lab integration

The Application Automation Tools plugin for the Jenkins continuous integration server provides a mechanism for uploading apps to the Digital Lab lab console.

First you define the Digital Lab server within Jenkins, and then you add build steps to upload your mobile apps with .apk (Android) or .ipa (iOS) file extensions.

For additional information, see the [Digital Lab Help Center](https://admhelp.microfocus.com/digitallab/en/).

### Table of Contents

[Prerequisites](#prerequisites)

[Define the Digital Lab server](#define-the-digital-lab-server)

[Use Digital Lab with SSL](#use-digital-lab-with-ssl)

[Upload apps to Digital Lab](#upload-apps-to-digital-lab)



## Prerequisites

1.  Install one of the five latest LTS versions of Jenkins, [(Click here for a list.)](https://jenkins.io/changelog-stable/)

2.  Install the Jenkins [Micro Focus Application Automation Tools plugin](https://plugins.jenkins.io/hp-application-automation-tools-plugin).



## Define the Digital Lab server 

Before using Jenkins with Digital Lab, you need to configure Jenkins to recognize the Digital Lab server.

To configure Jenkins to integrate with Digital Lab:

1. On the Jenkins Server home page, click **Manage Jenkins > Configure System**.

2. Go to the **Digital Lab** section, and click **Add Digital Lab server**.

3. Enter a name for the Digital Lab server that you will be using, and its URL.

4. Repeat the last two steps for each of the Digital Lab servers that you will be accessing.

5. For running functional tests where UFT and Jenkins are hosted on separate machines, you need to create an execution node for the functional test:

   a. Select **Manage Jenkins > Manage Nodes and Clouds > New Node**.

   b. Give the node a name, and select the **Permanent Agent** option.

   c. Enter the details for the UFT machine.

   d. Save your changes.

## Use Digital Lab with SSL

If you need to use Digital Lab securely, with SSL, you must first install the UFTM server certificate. 

1. Copy the UFTM server certificate to the Jenkins server machine.
2. Import the UFTM server certificate on the Jenkins server machine using the following command: 
   ```
    keytool.exe -import -file "<local_path>\<certificate_filename>.cer" 
     -keystore "C:\Program Files (x86)\Jenkins\jre\lib\security\cacerts" 
     -alias mc  -storepass changeit -noprompt 
3. Restart the Jenkins service.     

## Upload apps to Digital Lab 

The **Application Automation Tools** Jenkins plugin provides a standalone builder for uploading apps to Digital Lab. If you want to create a job that runs a UFT One functional test with Mobile devices, see the [UFT One Help Center](https://admhelp.microfocus.com/uft/en/latest/UFT_Help/Content/MC/mobile_on_UFT_Jenkins_integ.htm).

1. Make sure you have added your Digital Lab server to the Jenkins configuration as described in [Define the Digital Lab server](#define-the-digital-lab-server).
2. Copy your application package file, with **.apk** or **.ipa** extensions, to the Jenkins machine.
3. On the Jenkins Server home page, click **New Item**.
4. Enter an item name for the project.
5. Select **Free style project** and click **OK** in the bottom left corner.
6. In the **General** tab, scroll down to the **Build** section.
7. Expand the **Add build step** drop-down and select **Upload app to Digital Lab**.
8. Select your Digital Lab server from the drop-down list of servers.
9. Provide your login credentials. If your server has the multiple share spaces enabled, you must also include the nine-digit project ID in the **Tenant ID** field. If the feature is not enabled, you must leave this field empty.
10. If you are connecting to a Digital Lab server through a proxy, select **Use proxy settings** and provide the relevant information.
11. Click **Add Application** and enter the full path of the **.apk** or **.ipa** package file of the app you want to upload to the Digital Lab server. Repeat this step for each app you want to upload.
12. Click **Apply** to save your changes and continue with more build steps.
13. Click **Save** when you are finished adding build steps.
14. Run or trigger the job as you would with any standard Jenkins job.
15. To troubleshoot, check the log file on the Digital Lab server for issues such as connectivity and security.
