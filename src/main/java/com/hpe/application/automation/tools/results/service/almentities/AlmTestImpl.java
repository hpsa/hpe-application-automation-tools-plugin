package com.hpe.application.automation.tools.results.service.almentities;

public class AlmTestImpl extends AlmEntityImpl implements AlmTest {
	private static String restPrefix = "tests"; 
	public String getRestPrefix() {
		return restPrefix;
	}
	
	public String getKey(){
		String className = (String)getFieldValue(TS_UT_CLASS_NAME);
		
		String methodName = (String)getFieldValue(TS_UT_METHOD_NAME);
		String packageName = (String)getFieldValue(TS_UT_PACKAGE_NAME);
		if(packageName == null) {
			packageName = "";
		}
		String testingFramework = (String) getFieldValue(TS_TESTING_FRAMEWORK);
		String key = packageName +"_" +className +"_"+ methodName+"_"+testingFramework;
		return key;
	}
	
	public boolean equals(Object o){
		
		if( !(o instanceof AlmTestImpl)){
			return false;
		}
		
		if(this == o) {
			return true;
		}
		
		AlmTestImpl oT = (AlmTestImpl) o;
		
		String className = (String)getFieldValue(TS_UT_CLASS_NAME);
		
		String methodName = (String)getFieldValue(TS_UT_METHOD_NAME);
		String packageName = (String)getFieldValue(TS_UT_PACKAGE_NAME);
		if(packageName == null) {
			packageName = "";
		}
		
		String testingFramework = (String) getFieldValue(TS_TESTING_FRAMEWORK);
		
		return className.equals((String)oT.getFieldValue(TS_UT_CLASS_NAME))
				&& 	packageName.equals((String)oT.getFieldValue(TS_UT_PACKAGE_NAME))
				&& 	methodName.equals((String)oT.getFieldValue(TS_UT_METHOD_NAME))
				&& 	testingFramework.equals((String)oT.getFieldValue(TS_TESTING_FRAMEWORK));
		
	}
	
	public int hashCode(){

		return getKey().hashCode();
		
	}
}
