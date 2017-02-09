package hello;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HelloWorldTest {

    @Test
    public void testOne() {
        HelloWorld.main(new String[0]);
    }

    @Test
    public void testTwo() {
        Assert.fail("just because");
    }

    @Test(enabled = false)
    public void testThree() {
        Assert.fail("just because");
    }
}
