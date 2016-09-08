package ngalambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * A simple mock implementation of the {@code Context} interface. Default
 * values are stubbed out, and setters are provided so you can customize
 * the context before passing it to your function.
 */
public class TestContext implements Context {

	private String awsRequestId = "EXAMPLE";
	private ClientContext clientContext;
	private String functionName = "EXAMPLE";
	private CognitoIdentity identity;
	private String logGroupName = "EXAMPLE";
	private String logStreamName = "EXAMPLE";
    private final String arn = "arn:EXAMPLE::us-west-2";
	private final String version = "1.0.0.0";
	private LambdaLogger logger = new TestLogger();
	private int memoryLimitInMB = 128;
	private int remainingTimeInMillis = 15000;

	@Override
	public String getAwsRequestId() {
		return this.awsRequestId;
	}

	public void setAwsRequestId(final String value) {
		this.awsRequestId = value;
	}

	@Override
	public ClientContext getClientContext() {
		return this.clientContext;
	}

	public void setClientContext(final ClientContext value) {
		this.clientContext = value;
	}

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	public void setFunctionName(final String value) {
		this.functionName = value;
	}

	@Override
	public CognitoIdentity getIdentity() {
		return this.identity;
	}

	public void setIdentity(final CognitoIdentity value) {
		this.identity = value;
	}

	@Override
	public String getLogGroupName() {
		return this.logGroupName;
	}

	public void setLogGroupName(final String value) {
		this.logGroupName = value;
	}

	@Override
	public String getLogStreamName() {
		return this.logStreamName;
	}

	public void setLogStreamName(final String value) {
		this.logStreamName = value;
	}

	@Override
	public LambdaLogger getLogger() {
		return this.logger;
	}

	public void setLogger(final LambdaLogger value) {
		this.logger = value;
	}

	@Override
	public int getMemoryLimitInMB() {
		return this.memoryLimitInMB;
	}

	public void setMemoryLimitInMB(final int value) {
		this.memoryLimitInMB = value;
	}

	@Override
	public int getRemainingTimeInMillis() {
		return this.remainingTimeInMillis;
	}

	public void setRemainingTimeInMillis(final int value) {
		this.remainingTimeInMillis = value;
	}

	/**
	 * A simple {@code LambdaLogger} that prints everything to stderr.
	 */
	private static class TestLogger implements LambdaLogger {

		@Override
		public void log(final String message) {
			System.err.println(message);
		}
	}

	@Override
	public String getFunctionVersion() {
		return this.version;
	}

	@Override
	public String getInvokedFunctionArn() {
		return this.arn;
	}
}
