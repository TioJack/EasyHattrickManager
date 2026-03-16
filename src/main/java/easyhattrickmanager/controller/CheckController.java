package easyhattrickmanager.controller;

import easyhattrickmanager.service.CheckActiveService;
import easyhattrickmanager.service.CheckDataService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("check")
public class CheckController {

    private final CheckDataService checkDataService;
    private final CheckActiveService checkActiveService;

    @GetMapping("data")
    public ResponseEntity<Void> checkData() {
        try {
            checkDataService.checkData();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("active")
    public ResponseEntity<Void> checkActive() {
        try {
            checkActiveService.checkActive();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
