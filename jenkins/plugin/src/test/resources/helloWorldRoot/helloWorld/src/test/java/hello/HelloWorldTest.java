package hello;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class HelloWorldTest {

    @Test
    public void testOne() {
        HelloWorld.main(new String[0]);
    }

    @Test
    public void testTwo() {
        Assert.fail("just because");
    }

    @Test
    @Ignore
    public void testThree() {
        Assert.fail("just because");
    }
}
