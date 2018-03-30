package hello;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ITHelloWorld {

    @Test
    public void testOne() {
        HelloWorld.main(new String[0]);
    }

}
