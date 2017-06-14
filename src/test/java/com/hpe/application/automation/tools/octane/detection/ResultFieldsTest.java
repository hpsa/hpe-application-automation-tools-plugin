package com.hpe.application.automation.tools.octane.detection;

import com.hpe.application.automation.tools.octane.tests.detection.ResultFields;
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
