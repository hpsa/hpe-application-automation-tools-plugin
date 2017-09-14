package com.hpe.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 08/07/2016.
 */
public interface LrTest {
    public enum SLA_STATUS {
        Failed, Passed, NoData, bad;

        public static SLA_STATUS checkStatus(String status) {
            if ((status.compareTo(Failed.toString()) == 0)) {
                return Failed;
            } else if ((status.compareTo(Passed.toString()) == 0)) {
                return Passed;
            } else if ((status.compareTo(NoData.toString()) == 0)) {
                return NoData;
            }
            return bad;
        }
    }

    enum SLA_GOAL {
        AverageThroughput, TotalThroughput, AverageHitsPerSecond, TotalHits,
        ErrorsPerSecond, PercentileTRT, AverageTRT, Bad;

        public static SLA_GOAL checkGoal(String status) {
            if ((status.compareTo(AverageThroughput.toString()) == 0)) {
                return AverageThroughput;
            } else if ((status.compareTo(TotalThroughput.toString()) == 0)) {
                return TotalThroughput;
            } else if ((status.compareTo(AverageHitsPerSecond.toString()) == 0)) {
                return AverageHitsPerSecond;
            } else if ((status.compareTo(TotalHits.toString()) == 0)) {
                return TotalHits;
            } else if ((status.compareTo(ErrorsPerSecond.toString()) == 0)) {
                return ErrorsPerSecond;
            } else if ((status.compareTo(AverageTRT.toString()) == 0)) {
                return AverageTRT;
            } else if ((status.compareTo(PercentileTRT.toString()) == 0)) {
                return PercentileTRT;
            }
            return Bad;
        }

    }
}
