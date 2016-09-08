package ngalambda.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;

/**
 * Utility class for region-related tools.
 * 
 * @author Robert Roth
 *
 */
public final class RegionUtil {
    /**
     * Get the current region from a Lambda function context.
     * 
     * @param context
     *            the lambda context
     * @return the region as found in the ARN of the Lambda function
     */
    public static Regions getRegionFromContext(final Context context) {
        final String arn = context.getInvokedFunctionArn();
        final String[] split = arn.split(":");
        return Regions.fromName(split[3]);
    }

}
