package ngalambda;

import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class NgaLambdaTest {

    private static Map<String, ?> input;

    @SuppressWarnings("unchecked")
    @BeforeClass
	public static void createInput() throws IOException {
         input = TestUtils.parse("s3-event.put.json", Map.class);
	}

	private Context createContext() {
		final TestContext ctx = new TestContext();

		// TODO: customize your context here if needed.
		ctx.setFunctionName("Your Function Name");
		return ctx;
	}

    // @Test
	public void testLambdaFunctionHandler() {
		final NgaConfig handler = new NgaConfig();
		final Context ctx = createContext();

		final Object output = handler.handleRequest(input, ctx);

		// TODO: validate output here if needed.
		if (output != null) {
			System.out.println(output.toString());
		}
	}
}
