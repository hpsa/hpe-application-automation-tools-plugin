package ngalambda;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.Job;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import ngalambda.aws.NgaCodePipelineClient;

/**
 * Job for processing an NGA config custom action. To be used alone invoked by
 * S3 PUT events for the source artifacts.
 *
 * @author Robert Roth
 *
 */
public class NgaConfig extends AbstractNgaLambda {

    public NgaConfig() {
        super();
    }

    /**
     * Handle a request: find the NGA custom actions to be processed, and
     * process them one by one. If no jobs found to process, throw an
     * IllegalStateException to trigger an automatic reschedule (Lambda
     * functions are rescheduled three times) to retry 15 seconds later to
     * resolve occasional delay in job status update after the previous job has
     * finished.
     */
    @Override
    public Object handleRequest(final Map<String, ?> input, final Context context) {
        setUpFromContext(context);
        final List<Job> jobs = this.client.pollForJobs(ActionCategory.Build, ActionOwner.Custom, "NextGenALM", "5");
        if (jobs.size() == 0) {
            throw new IllegalStateException("No jobs found!!!");
        }
        for (final Job job : jobs) {
            processJob(this.client, this.logger, job);
        }
        return Boolean.TRUE;
    }

    public NgaConfig(final Context context, final NgaCodePipelineClient client) {
        super(context, client, new LambdaLogger() {

            @Override
            public void log(final String string) {
                System.out.println(string);
            }
        });
    }

}
