package easyhattrickmanager.utils;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static void downloadFile(String fileUrl, String destinationPath) {
        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.createDirectories(Paths.get(destinationPath).getParent());
            Files.copy(in, Paths.get(destinationPath));
        } catch (Exception e) {
            System.err.printf("Error downloadFile %s %s %s%n", fileUrl, destinationPath, e.getMessage());
        }
    }
}
