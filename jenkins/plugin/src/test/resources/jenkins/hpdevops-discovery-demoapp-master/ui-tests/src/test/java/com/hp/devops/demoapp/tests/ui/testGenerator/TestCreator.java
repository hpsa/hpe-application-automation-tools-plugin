package com.hp.devops.demoapp.tests.ui.testGenerator;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.Arrays;

/**
 * User: belozovs
 * Date: 1/1/15
 * Description:
 *      Run this class to generate a {@link TestCreator#numOfTests} tests and run (or not - {@link TestCreator#shouldRun}) them together with TestA and TestB.
 *      The test output is printed to this class stdout
 *      The test source is copied from TestA with 3 differences:
 *      - package changed
 *      - import com.hp.devops.demoapp.tests.ui.SpecialTests added
 *      - TestA suffix added to the test method names
 */
public class TestCreator {

    private static int numOfTests = 3;
    private static boolean shouldRun = true;

    private static String sourceCode = "package com.hp.devops.demoapp.tests.ui.generated;\n" +
            "\n" +
            "import com.hp.devops.demoapp.tests.ui.SpecialTests;\n" +
            "import org.junit.AfterClass;\n" +
            "import org.junit.Assert;\n" +
            "import org.junit.BeforeClass;\n" +
            "import org.junit.Test;\n" +
            "import org.junit.experimental.categories.Category;\n" +
            "import org.openqa.selenium.By;\n" +
            "import org.openqa.selenium.Proxy;\n" +
            "import org.openqa.selenium.WebDriver;\n" +
            "import org.openqa.selenium.WebElement;\n" +
            "import org.openqa.selenium.htmlunit.HtmlUnitDriver;\n" +
            "import org.openqa.selenium.remote.CapabilityType;\n" +
            "import org.openqa.selenium.remote.DesiredCapabilities;\n" +
            "\n" +
            "public class TestA {\n" +
            "\n" +
            "    static final boolean isMusicApp = false;\n" +
            "\n" +
            "    static private WebDriver driver;\n" +
            "    static private boolean isBehindProxy = false;\n" +
            "    static private String testProxy;\n" +
            "    static private String appUrl;\n" +
            "\n" +
            "    @BeforeClass\n" +
            "    static public void beforeAll() {\n" +
            "\n" +
            "        if(isMusicApp){\n" +
            "            testProxy = \"web-proxy.bbn.hp.com:8081\";\n" +
            "            appUrl = \"http://54.146.140.70:9000\";\n" +
            "        } else {\n" +
            "            testProxy = \"\";\n" +
            "            appUrl = \"http://myd-vm02771.hpswlabs.adapps.hp.com:8080/jenkins\";\n" +
            "        }\n" +
            "\n" +
            "        if (\"true\".equals(System.getProperty(\"proxy\"))) {\n" +
            "            isBehindProxy = true;\n" +
            "            System.out.println(\"isBehindProxy is true!\");\n" +
            "            if (System.getenv(\"testproxy\") != null) {\n" +
            "                testProxy = System.getenv(\"testproxy\");\n" +
            "            }\n" +
            "            System.out.println(\"testProxy is \" + testProxy + \"; can be modified via environment variable, i.e., 'export testproxy=web-proxy.bbn.hp.com:8080'\");\n" +
            "        }\n" +
            "        else {\n" +
            "            System.out.println(\"We do not use proxy\");\n" +
            "        }\n" +
            "\n" +
            "        if (isBehindProxy) {\n" +
            "            Proxy proxy = new Proxy();\n" +
            "            proxy.setHttpProxy(testProxy);\n" +
            "            DesiredCapabilities cap = new DesiredCapabilities();\n" +
            "            cap.setCapability(CapabilityType.PROXY, proxy);\n" +
            "            driver = new HtmlUnitDriver(cap);\n" +
            "        }\n" +
            "        else {\n" +
            "            driver = new HtmlUnitDriver();\n" +
            "        }\n" +
            "        if (System.getProperty(\"appUrl\") != null) {\n" +
            "            appUrl = System.getProperty(\"appUrl\");\n" +
            "        }\n" +
            "        System.out.println(\"App URL is \" + appUrl + \"; can be modifed via system property, i.e., '-DappUrl=\\\"http://54.146.140.70:9000\\\"'\");\n" +
            "\n" +
            "        driver.get(appUrl);\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    @Test\n" +
            "    public void testUIcaseATestA() {\n" +
            "        System.out.println(\"Proudly running test \" + Thread.currentThread().getStackTrace()[1]);\n" +
            "        WebElement query;\n" +
            "        if(isMusicApp){\n" +
            "            query = driver.findElement(By.id(\"bandsList\"));\n" +
            "            Assert.assertEquals(query.getTagName(), \"div\");\n" +
            "        } else {\n" +
            "            query = driver.findElement(By.id(\"jenkins\"));\n" +
            "            Assert.assertEquals(query.getTagName(), \"body\");\n" +
            "        }\n" +
            "        Assert.assertEquals(query.isDisplayed(), true);\n" +
            "    }\n" +
            "\n" +
            "    @Category(SpecialTests.class)\n" +
            "    @Test\n" +
            "    public void testUIcaseBTestA() {\n" +
            "        System.out.println(\"Proudly running test \" + Thread.currentThread().getStackTrace()[1]);\n" +
            "        WebElement query;\n" +
            "        if(isMusicApp){\n" +
            "            query = driver.findElement(By.id(\"totalVotes\"));\n" +
            "            Assert.assertEquals(query.getTagName(), \"div\");\n" +
            "        } else {\n" +
            "            query = driver.findElement(By.id(\"jenkins\"));\n" +
            "            Assert.assertEquals(query.getTagName(), \"body\");\n" +
            "        }\n" +
            "        Assert.assertEquals(query.isDisplayed(), true);\n" +
            "    }\n" +
            "\n" +
            "    @AfterClass\n" +
            "    static public void afterAll() {\n" +
            "        driver.quit();\n" +
            "    }\n" +
            "}\n";


    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {

        final String CLASS_NAME_STRING = "GeneratedTest";
        final String MODULE_STRING = "./ui-tests/src/test/java/";
        final String PACKAGE_STRING = "com.hp.devops.demoapp.tests.ui.generated.";

        final String CASEA_STRING = "testUIcaseAGeneratedTest";
        final String CASEB_STRING = "testUIcaseBGeneratedTest";

        StringBuffer commandLine = new StringBuffer("-Dtest=com.hp.devops.demoapp.tests.ui.TestA#testUIcaseA+testUIcaseB,com.hp.devops.demoapp.tests.ui.TestB#testUIcaseC+testUIcaseD,");

        for (int i = 0; i < numOfTests; i++) {
            File testFile = new File(MODULE_STRING + PACKAGE_STRING.replace(".", "/") + CLASS_NAME_STRING + i + ".java");
            FileWriter fileWriter = new FileWriter(testFile);
            String mySource = sourceCode.replace("TestA", CLASS_NAME_STRING + i);
            fileWriter.write(mySource);
            fileWriter.close();

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File(MODULE_STRING + PACKAGE_STRING.replace(".", "/"))));
            compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjectsFromFiles(Arrays.asList(testFile))).call();
            fileManager.close();

            commandLine.append(PACKAGE_STRING).append(CLASS_NAME_STRING).append(i).append("#").append(CASEA_STRING).append(i).append("+").append(CASEB_STRING).append(i).append(",");

        }

        String testParameter = commandLine.substring(0, commandLine.length() - 1);
        System.out.println("Generated test parameter: " + testParameter);
        System.out.println("Test parameter length is " + testParameter.length() + " characters");

        if (shouldRun) {
            System.out.println("Process exitValue is " + runIt(testParameter));
        }
    }

    public static int runIt(String testParameter) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("C:/Java/Maven/apache-maven-3.2.2/bin/mvn.bat clean install -f ./ui-tests/pom.xml " + testParameter);

        String line = "";
        InputStream is = pr.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }

        return pr.waitFor();
    }


}
