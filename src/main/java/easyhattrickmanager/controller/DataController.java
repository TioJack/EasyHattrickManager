package easyhattrickmanager.controller;

import easyhattrickmanager.controller.model.DataResponse;
import easyhattrickmanager.service.DataService;
import easyhattrickmanager.service.model.dataresponse.UserConfig;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("data")
public class DataController {

    private final DataService dataService;

    @GetMapping
    public ResponseEntity<DataResponse> getData() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        DataResponse dataResponse = dataService.getData(username);
        return ResponseEntity.ok(dataResponse);
    }

    @PostMapping("/user-config")
    public ResponseEntity<Void> saveUserConfig(@RequestBody UserConfig userConfig) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        dataService.saveUserConfig(username, userConfig);
        return ResponseEntity.ok().build();
    }

}
