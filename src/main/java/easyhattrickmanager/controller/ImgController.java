package easyhattrickmanager.controller;

import static easyhattrickmanager.utils.FileUtils.downloadFile;

import easyhattrickmanager.configuration.AssetsConfiguration;
import easyhattrickmanager.repository.CountryDAO;
import easyhattrickmanager.service.UpdateService;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("img")
public class ImgController {

    private final CountryDAO countryDAO;
    private final AssetsConfiguration assetsConfiguration;
    private final UpdateService updateService;

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

    @GetMapping("avatar")
    public ResponseEntity<Void> getAvatar(@RequestParam("playerId") int playerId) {
        updateService.getAvatar(playerId);
        return ResponseEntity.ok().build();
    }
}
