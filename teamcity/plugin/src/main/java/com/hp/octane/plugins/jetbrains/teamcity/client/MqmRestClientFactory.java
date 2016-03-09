//public class MqmRestClientFactory {
//
//	private static String host;
//	private static Integer port;
//	private static String userName;
//	private static String password;
//
//	public static MqmRestClient create(String clientType, String location, String sharedSpace, String username, String password) {
//		MqmConnectionConfig clientConfig = new MqmConnectionConfig(location, sharedSpace, username, password, clientType);
//		URL locationUrl = null;
//		try {
//			locationUrl = new URL(clientConfig.getLocation());
//		} catch (MalformedURLException e) {
//			throw new IllegalArgumentException(e);
//		}
//		configureProxy(clientType, locationUrl, clientConfig, username);
//
//		return new MqmRestClientImpl(clientConfig);
//	}
//
//	private static void configureProxy(String clientType, URL locationUrl, MqmConnectionConfig clientConfig, String username) {
//		if (clientType.equals(ConfigurationService.CLIENT_TYPE)) {
//			if (isProxyNeeded(locationUrl.getHost())) {
//				clientConfig.setProxyHost(getProxyHost());
//				clientConfig.setProxyPort(getProxyPort());
//				final String proxyUsername = getUsername();
//				if (!proxyUsername.isEmpty()) {
//					clientConfig.setProxyCredentials(new UsernamePasswordProxyCredentials(username, getPassword()));
//				}
//			}
//
//		}
//	}
//
//
//	private static boolean isProxyNeeded(final String str) {
//		Map<String, String> propertiesMap = parseProperties(System.getenv("TEAMCITY_SERVER_OPTS"));
//
//		if (propertiesMap.get("Dhttps.proxyHost") == null) {
//			return false;
//		}
//		host = propertiesMap.get("Dhttps.proxyHost");
//		if (propertiesMap.get("Dhttps.proxyPort") != null) {
//			port = Integer.parseInt(propertiesMap.get("Dhttps.proxyPort"));
//		}
//
//		return true;
///*
//                -Dproxyset=true
//                -Dhttp.proxyHost=proxy.domain.com
//                -Dhttp.proxyPort=8080
//                -Dhttp.nonProxyHosts=domain.com
//                -Dhttps.proxyHost=web-proxy.il.hpecorp.net
//                -Dhttps.proxyPort=8080
//                -Dhttps.nonProxyHosts=domain.com
//                */
//	}
//
//	private static Map<String, String> parseProperties(String internalProperties) {
//		Map<String, String> propertiesMap = new HashMap<String, String>();
//		if (internalProperties != null) {
//			String[] properties = internalProperties.split(" -");
//			for (String str : Arrays.asList(properties)) {
//				String[] split = str.split("=");
//				if (split.length == 2) {
//					propertiesMap.put(split[0], split[1]);
//				}
//			}
//		}
//		return propertiesMap;
//	}
//}
