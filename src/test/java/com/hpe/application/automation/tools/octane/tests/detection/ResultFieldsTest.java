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

package com.hpe.application.automation.tools.octane.tests.detection;

import org.junit.Assert;
import org.junit.Test;

public class ResultFieldsTest {

    @Test
    public void testEquals() {
        ResultFields x = new ResultFields("foo", "bar", null);
        Assert.assertTrue(x.equals(x));
        Assert.assertTrue(!x.equals(null));

        ResultFields y = new ResultFields("foo", "bar", null);
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());

        ResultFields z = new ResultFields("foo", "bar", "");
        Assert.assertTrue(!x.equals(z) && !z.equals(x));
    }
}
