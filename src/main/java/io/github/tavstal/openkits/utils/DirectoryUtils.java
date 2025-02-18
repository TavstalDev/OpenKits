package io.github.tavstal.openkits.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryUtils {
    /**
     * Checks if a directory is empty.
     *
     * @param dirPath The path to the directory.
     * @return true if the directory is empty, false otherwise.
     */
    public static boolean isDirectoryEmpty(Path dirPath) {
        if (Files.notExists(dirPath)) {
            return true;
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            return !directoryStream.iterator().hasNext();
        }
        catch (IOException ex) {
            LoggerUtils.LogError("Failed to check if directory is empty: ");
            LoggerUtils.LogError(ex.getMessage());
            return  false;
        }
    }
}
