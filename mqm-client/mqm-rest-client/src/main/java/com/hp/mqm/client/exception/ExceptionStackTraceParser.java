// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.exception;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Attempts to parse serialized java exception stack trace into a Throwable in order to retain server-side stack trace
 * information as part of the client-side exceptions.
 */
public class ExceptionStackTraceParser {

    private ExceptionStackTraceParser() {
    }

    /**
     * Parse stack trace element from single stack trace line.
     * Retuns null if cannot parse.
     *
     * @param line line to parse
     * @return parsed stack trace element or null if "wrong format" of line
     */
    private static StackTraceElement parseStackTraceElement(String line) {
        // JDK line:
        //   \tat class-name.method-name(Native method | file:line | file | Unknown source)\n
        // JRockit line:
        //   \tat class-name.method-name(method-signature[:???])(Native method | file:line | file | Unknown source)\n
        if (!line.startsWith("at ")) {
            return null; // NON-NLS
        }
        String className, methodName, fileName;
        int lineNo;
        // Discard 'at'
        line = line.substring(3);
        // Split by last '(' (because of JRockit)
        int i = line.lastIndexOf('(');
        if (i == -1) {
            return null;
        }
        String left = line.substring(0, i);
        String right = line.substring(i + 1);
        // Split into class and method
        i = left.lastIndexOf('.');
        if (i == -1) {
            className = left;
            methodName = "";
        } else {
            className = left.substring(0, i);
            methodName = left.substring(i + 1);
        }
        // Split into file and line number
        i = right.indexOf(')');
        if (i == -1) {
            return null;
        }
        right = right.substring(0, i);
        i = right.lastIndexOf(':');
        if (right.startsWith("Native Method")) { // NON-NLS
            fileName = null;
            lineNo = -2;
        } else if (right.startsWith("Unknown Source")) { // NON-NLS
            fileName = null;
            lineNo = -1;
        } else if (i == -1) {
            fileName = right;
            lineNo = -1;
        } else {
            fileName = right.substring(0, i);
            try {
                lineNo = Integer.parseInt(right.substring(i + 1));
            } catch (NumberFormatException e) {
                lineNo = -1;
            }
        }
        return new StackTraceElement(className, methodName, fileName, lineNo);
    }

    /**
     * Parse exception from serialized stack trace.
     *
     * @param s string containing stack trace
     * @return parsed exception or null if failed
     */
    public static Throwable parseException(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s, "\n\r");
        if (!tokenizer.hasMoreTokens()) {
            return null;
        }
        return parseException(tokenizer.nextToken(), tokenizer, new LinkedList<StackTraceElement>());
    }

    /**
     * Do the real stack trace import. Called recursivelly for 'Caused by' exceptions.
     *
     * @param firstLine         first line of stack trace
     * @param tokenizer         rest of stack trace as tokenizer
     * @param superStackTrace   stack trace of wrapping exception
     * @return parsed exception
     */
    private static Throwable parseException(String firstLine, StringTokenizer tokenizer, List<StackTraceElement> superStackTrace) {

        // Format:
        //   exc-class[: exc-message]\n
        //   \tat class-name.method-name(Native method | file:line | file | Unknown source)\n
        //   \t... nn more\n
        //   Caused by: ...

        // Gather multiline message here
        StringBuilder message = new StringBuilder();
        message.append(firstLine);
        boolean inMessage = true;
        // Gather stack trace elements here
        List<StackTraceElement> list = new LinkedList<StackTraceElement>();
        // Cause here - if any
        Throwable cause = null;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            StackTraceElement e = parseStackTraceElement(token);
            if (e != null) {
                // Stack trace element read - message finished
                inMessage = false;
                list.add(e);
            } else if (token.startsWith("Caused by: ") || token.startsWith("wrapped in ")) { // NON-NLS
                // Import cause
                token = token.substring(11);
                list.addAll(superStackTrace);
                cause = parseException(token, tokenizer, list);
                // End
                break;
            } else if (token.startsWith("... ")) {
                // Seems like "... nn more" - just skip it
                inMessage = false;
            } else {
                // Some other text - either message continuation or error (stop parsing)
                if (inMessage) {
                    message.append('\n');
                    message.append(token);
                } else {
                    break;
                }
            }
        }
        // Extract exception class name and message
        int i = message.indexOf(":");
        Throwable exception;
        if (i == -1) {
            exception = new ParsedException(message.toString(), null);
        } else {
            exception = new ParsedException(message.substring(0, i), message.substring(i + 1).trim());
        }
        // Initialize exception
        if (cause == null) {
            // Super stack trace not imported yet
            list.addAll(superStackTrace);
        } else {
            // Already imported when cause was created
            exception.initCause(cause);
        }
        exception.setStackTrace(list.toArray(new StackTraceElement[list.size()]));
        return exception;
    }

    /**
     * Exception parsed from stack trace. Overrides toString() method
     * to mimic original exception by hiding own class name.
     */
    private static class ParsedException extends Throwable {
        private static final long serialVersionUID = -549126552723814501L;

        private String className;

        ParsedException(String className, String message) {
            super(message);
            this.className = className;
        }

        public String toString() {
            String message = getMessage();
            return (message != null) ? (className + ": " + message) : className;
        }
    }
}
