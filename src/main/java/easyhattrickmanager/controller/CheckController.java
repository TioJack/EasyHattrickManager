package easyhattrickmanager.controller;

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

    @GetMapping("data")
    public ResponseEntity<Void> checkData() {
        try {
            checkDataService.checkData();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
