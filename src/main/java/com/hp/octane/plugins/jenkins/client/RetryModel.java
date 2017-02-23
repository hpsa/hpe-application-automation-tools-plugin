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

package com.hp.octane.plugins.jenkins.client;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;
import hudson.util.TimeUnit2;

@Extension
public class RetryModel implements ConfigurationListener {

    private static final long[] QUIET_PERIOD = { // TODO: janotav: verify against our Saas policy
            TimeUnit2.MINUTES.toMillis(1),
            TimeUnit2.MINUTES.toMillis(10),
            TimeUnit2.MINUTES.toMillis(60)
    };

    private long boundary;
    private int periodIndex;

    private TimeProvider timeProvider = new SystemTimeProvider();
    private EventPublisher eventPublisher;

    @Inject
    public RetryModel() {
        doSuccess();
    }


    public RetryModel(EventPublisher eventPublisher) {
        this();
        this.eventPublisher = eventPublisher;
    }

    public synchronized boolean isQuietPeriod() {
        return timeProvider.getTime() < boundary;
    }

    public synchronized void failure() {
        if (periodIndex < QUIET_PERIOD.length - 1) {
            periodIndex++;
        }
        boundary = timeProvider.getTime() + QUIET_PERIOD[periodIndex];
    }

    public void success() {
        doSuccess();
        eventPublisher.resume();
    }

    private synchronized void doSuccess() {
        periodIndex = -1;
        boundary = 0;
    }

    @Override
    public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
        doSuccess();
    }

    @Inject
    public void setEventPublisher(JenkinsInsightEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    private static class SystemTimeProvider implements TimeProvider {

        @Override
        public long getTime() {
            return System.currentTimeMillis();
        }
    }

    interface TimeProvider {

        long getTime();

    }

}
