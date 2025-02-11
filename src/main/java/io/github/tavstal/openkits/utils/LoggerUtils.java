package io.github.tavstal.openkits.utils;

import io.github.tavstal.openkits.OpenKits;

import java.util.logging.Logger;

/**
 * Utility class for logging messages with different severity levels.
 */
public class LoggerUtils {
    private static final Logger _logger = Logger.getLogger(OpenKits.PROJECT_NAME);

    /**
     * Logs an informational message.
     *
     * @param text the message to log.
     */
    public static void LogInfo(String text) {
        _logger.log(java.util.logging.Level.INFO, String.format("%s", text));
    }

    /**
     * Logs a warning message.
     *
     * @param text the message to log.
     */
    public static void LogWarning(String text) {
        _logger.log(java.util.logging.Level.WARNING, String.format("%s", text));
    }

    /**
     * Logs an error message.
     *
     * @param text the message to log.
     */
    public static void LogError(String text) {
        _logger.log(java.util.logging.Level.SEVERE, String.format("%s", text));
    }

    /**
     * Logs a debug message if debugging is enabled in the configuration.
     *
     * @param text the message to log.
     */
    public static void LogDebug(String text) {
        if (OpenKits.Instance.getConfig().getBoolean("debug"))
            _logger.log(java.util.logging.Level.INFO, String.format("%s", text));
    }
}
