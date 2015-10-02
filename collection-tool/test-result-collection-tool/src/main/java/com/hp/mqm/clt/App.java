package com.hp.mqm.clt;

public class App {

    public static void main(String[] args) {
        CliParser cliParser = new CliParser();
        Settings settings = cliParser.parse(args);
        TestResultCollectionTool testResultCollectionTool = new TestResultCollectionTool(settings);
        testResultCollectionTool.collectAndPushTestResults();
    }
}
