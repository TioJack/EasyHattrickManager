package easyhattrickmanager.controller;

import easyhattrickmanager.configuration.AssetsConfiguration;
import easyhattrickmanager.repository.CountryDAO;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("img")
public class ImgController {

    private final CountryDAO countryDAO;
    private final AssetsConfiguration assetsConfiguration;

    @GetMapping("flags")
    public ResponseEntity<Void> getFlags() {
        try {
            countryDAO.getAllLeagueCountry().forEach(leagueCountry -> {
                String imageUrl = assetsConfiguration.getHattrickUrl() + "/Img/flags/" + leagueCountry.getLeagueId() + ".png";
                String destinationPath = assetsConfiguration.getAssetsPath() + "/flags/" + leagueCountry.getCountryId() + ".png";
                if (!Files.exists(Paths.get(destinationPath))) {
                    downloadFile(imageUrl, destinationPath);
                }
            });
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void downloadFile(String fileUrl, String destinationPath) {
        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.createDirectories(Paths.get(destinationPath).getParent());
            Files.copy(in, Paths.get(destinationPath));
        } catch (Exception e) {
            System.err.printf("Error downloadFile %s %s %s%n", fileUrl, destinationPath, e.getMessage());
        }
    }
}
