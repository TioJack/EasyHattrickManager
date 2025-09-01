package easyhattrickmanager.controller;

import easyhattrickmanager.service.UpdateService;
import easyhattrickmanager.service.UpdateTranslationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("update")
public class UpdateController {

    private final UpdateService updateService;
    private final UpdateTranslationService updateTranslationService;

    @GetMapping
    public ResponseEntity<Void> update() {
        updateService.update();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team")
    public ResponseEntity<Void> updateTeam(@RequestParam("id") int teamId) {
        updateService.update(teamId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/leagues")
    public ResponseEntity<Void> updateLeagues() {
        updateService.updateLeagues();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/translationsHT")
    public ResponseEntity<Void> updateTranslationsHT() {
        updateTranslationService.updateTranslationsHT();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/translationsEHM")
    public ResponseEntity<Void> updateTranslationsEHM() {
        updateTranslationService.updateTranslationsEHM();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/i18n")
    public ResponseEntity<Void> updateI18n() {
        updateTranslationService.updateI18n();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/htms")
    public ResponseEntity<Void> updateHTMS() {
        updateService.updateHTMS();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/extendStaff")
    public ResponseEntity<Void> extendStaff(@RequestParam("teamId") int teamId) {
        updateService.extendStaff(teamId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/userConfig")
    public ResponseEntity<Void> updateUserConfig() {
        updateService.updateUserConfig();
        return ResponseEntity.ok().build();
    }

}
