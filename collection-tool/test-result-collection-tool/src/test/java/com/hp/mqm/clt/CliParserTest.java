package com.hp.mqm.clt;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CliParserTest {

    @Test
    public void testHelp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CliParser parser = new CliParser();
        Method help = parser.getClass().getDeclaredMethod("printHelp");
        help.setAccessible(true);
        help.invoke(parser);
    }

    @Test
    public void testVersion() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CliParser parser = new CliParser();
        Method help = parser.getClass().getDeclaredMethod("printVersion");
        help.setAccessible(true);
        help.invoke(parser);
    }
}
