package com.hp.mqm.atrf.core.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by berkovir on 20/12/2016.
 */
public class ConfigurationUtilities {

    private static boolean writeToLastSentRun = true;

    static final Logger logger = LogManager.getLogger();
    public static String LAST_SENT_FILE_PATH = "logs/lastSent.txt";

    public static String readLastSentRunId() {
        String value = null;
        Path path = Paths.get(LAST_SENT_FILE_PATH);
        if (Files.exists(path)) {
            try {
                value = new String(Files.readAllBytes(Paths.get(LAST_SENT_FILE_PATH))).trim();
                int intValue = Integer.parseInt(value);

            } catch (IOException e) {
                logger.error(String.format("Failed to read LastSentRunId from %s : %s", path.toFile().getAbsolutePath()), e.getMessage());
            } catch (NumberFormatException e) {
                logger.error(String.format("Failed to parse content of LastSentRunId from %s : %s", path.toFile().getAbsolutePath()), e.getMessage());
            }
        }

        return value;
    }

    public static void saveLastSentRunId(String lastSentId) {

        if (writeToLastSentRun) {
            Path path = Paths.get(LAST_SENT_FILE_PATH);
            try {
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                Files.write(path, lastSentId.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

            } catch (IOException e) {
                logger.error(String.format("Failed to write LAST_SENT_ID to %s", path.toFile().getAbsolutePath()));
                writeToLastSentRun = false;
            } catch (Exception e) {
                logger.error(String.format("Exception occurred during writing LAST_SENT_ID to %s : %s", path.toFile().getAbsolutePath()), e.getMessage());
                writeToLastSentRun = false;
            }
        }
    }
}
