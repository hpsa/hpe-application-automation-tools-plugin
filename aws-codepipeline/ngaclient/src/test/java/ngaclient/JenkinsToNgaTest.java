//package ngaclient;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.zip.ZipInputStream;
//
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.junit.Test;
//import org.xml.sax.SAXException;
//
//import ngaclient.BuildInfo.BuildResult;
//import ngaclient.BuildInfo.BuildStatus;
//
//public class JenkinsToNgaTest {
//
//	private BuildInfo createBuiildInfo() {
//		BuildInfo buildInfo = new BuildInfo();
//		buildInfo.setServerCiId("1001");
//		buildInfo.setJobInfo(new JobInfo("1001"));
//		buildInfo.setBuildCiId("1001");
//		buildInfo.setBuildName("Test");
//		buildInfo.setStartTime(Instant.now());
//		buildInfo.setDuration(Duration.ofMinutes(2));
//		buildInfo.setStatus(BuildStatus.FINISHED);
//		buildInfo.setResult(BuildResult.SUCCESS);
//		buildInfo.setCauses(new BuildCause[] { new BuildCause("1002", "1002") });
//		return buildInfo;
//	}
//
//	@Test
//	public void test1() throws IOException, SAXException, ParserConfigurationException {
//		try (InputStream jenkinsFile = getClass().getResourceAsStream("/ngaclient/tests1.zip")) {
//			JenkinsToNga.transform(createBuiildInfo(), new ZipInputStream(jenkinsFile), System.out);
//		}
//	}
//
//	@Test
//	public void test2() throws IOException, SAXException, ParserConfigurationException {
//		try (InputStream jenkinsFile = getClass().getResourceAsStream("/ngaclient/tests2.zip")) {
//			JenkinsToNga.transform(createBuiildInfo(), new ZipInputStream(jenkinsFile), System.out);
//		}
//	}
//
//	@Test
//	public void test3() throws IOException, SAXException, ParserConfigurationException {
//		try (InputStream jenkinsFile = getClass().getResourceAsStream("/ngaclient/tests3.zip")) {
//			JenkinsToNga.transform(createBuiildInfo(), new ZipInputStream(jenkinsFile), System.out);
//		}
//	}
//}
