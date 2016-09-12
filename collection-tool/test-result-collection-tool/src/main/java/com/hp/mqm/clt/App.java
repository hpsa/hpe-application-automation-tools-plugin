package com.hp.mqm.clt;

public class App {

    public static void main(String[] args) {
        CliParser cliParser = new CliParser();
        Settings settings = cliParser.parse(args);
        collectAndPushTestResults(settings);
    }

    public static void collectAndPushTestResults(Settings settings) {
        TestResultCollectionTool testResultCollectionTool = new TestResultCollectionTool(settings);
        testResultCollectionTool.collectAndPushTestResults();
    }
}
