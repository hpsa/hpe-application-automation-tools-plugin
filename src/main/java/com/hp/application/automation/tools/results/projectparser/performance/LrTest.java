package com.hp.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 08/07/2016.
 */
public interface LrTest {
    public enum SLA_STATUS {
        fail, pass, NoData, bad;

        public static SLA_STATUS checkStatus(String status)
        {
            if ((status.compareTo(fail.toString())==0))
            {
                return fail;
            }
            else if( (status.compareTo(pass.toString())==0))
            {
                return pass;
            }
            else if((status.compareTo(NoData.toString())==0))
            {
                return NoData;
            }
            return bad;
        }
    }

    enum SLA_GOAL {
        AverageThroughput, TotalThroughput, AverageHitsPerSecond, TotalHits,
        ErrorsPerSecond, AverageTRT, Bad;

        public static SLA_GOAL checkGoal(String status)
        {
            if ((status.compareTo(AverageThroughput.toString())==0))
            {
                return AverageThroughput;
            }
            else if((status.compareTo(TotalThroughput.toString())==0))
            {
                return TotalThroughput;
            }
            else if((status.compareTo(AverageHitsPerSecond.toString())==0))
            {
                return AverageHitsPerSecond;
            }
            else if((status.compareTo(TotalHits.toString())==0))
            {
                return TotalHits;
            }
            else if((status.compareTo(ErrorsPerSecond.toString())==0))
            {
                return ErrorsPerSecond;
            }
            else if((status.compareTo(AverageTRT.toString())==0))
            {
                return AverageTRT;
            }
            return Bad;
        }

    };
}
