package ut.com.emyoli.nga;

import org.junit.Test;
import com.emyoli.nga.api.MyPluginComponent;
import com.emyoli.nga.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}