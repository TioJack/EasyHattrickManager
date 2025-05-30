package easyhattrickmanager.controller;

import easyhattrickmanager.service.UpdateService;
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

    @GetMapping
    public ResponseEntity<Void> update(@RequestParam("teamId") int teamId) {
        updateService.update(teamId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/leagues")
    public ResponseEntity<Void> updateLeagues() {
        updateService.updateLeagues();
        return ResponseEntity.ok().build();
    }

}
