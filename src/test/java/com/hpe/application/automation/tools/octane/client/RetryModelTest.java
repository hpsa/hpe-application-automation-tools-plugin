/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.client;

import hudson.util.TimeUnit2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("squid:S2699")
public class RetryModelTest {

    private RetryModel retryModel;
    private TestTimeProvider testTimeProvider;
    private TestEventPublisher testEventPublisher;

    @Before
    public void init() {
        testEventPublisher = new TestEventPublisher();
        retryModel = new RetryModel(testEventPublisher);
        testTimeProvider = new TestTimeProvider();
        retryModel.setTimeProvider(testTimeProvider);
    }

    @Test
    public void testRetryModel() {
        Assert.assertFalse(retryModel.isQuietPeriod());

        retryModel.failure();
        // 1 minute
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.SECONDS.toMillis(25));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.SECONDS.toMillis(25));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.SECONDS.toMillis(25));
        Assert.assertFalse(retryModel.isQuietPeriod());

        retryModel.failure();
        // 10 minutes
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(4));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(4));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(4));
        Assert.assertFalse(retryModel.isQuietPeriod());

        retryModel.failure();
        // 60 minutes
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(25));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(25));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(25));
        Assert.assertFalse(retryModel.isQuietPeriod());

        retryModel.failure();
        // 60 minutes
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(25));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(25));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.MINUTES.toMillis(25));
        Assert.assertFalse(retryModel.isQuietPeriod());
    }

    @Test
    public void testRetryModelSuccess() {
        Assert.assertFalse(retryModel.isQuietPeriod());

        retryModel.failure();
        // 1 minute
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.SECONDS.toMillis(25));
        Assert.assertTrue(retryModel.isQuietPeriod());
        testTimeProvider.addOffset(TimeUnit2.SECONDS.toMillis(25));
        Assert.assertTrue(retryModel.isQuietPeriod());
        Assert.assertEquals(0, testEventPublisher.getResumeCount());
        retryModel.success();
        Assert.assertFalse(retryModel.isQuietPeriod());
        Assert.assertEquals(1, testEventPublisher.getResumeCount());
    }

    private static class TestTimeProvider implements RetryModel.TimeProvider {

        private long time = System.currentTimeMillis();

        public void addOffset(long time) {
            this.time += time;
        }

        @Override
        public long getTime() {
            return time;
        }
    }
}
