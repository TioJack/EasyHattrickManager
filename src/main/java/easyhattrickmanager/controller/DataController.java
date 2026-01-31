package easyhattrickmanager.controller;

import easyhattrickmanager.controller.model.DataResponse;
import easyhattrickmanager.controller.model.PlayerDataResponse;
import easyhattrickmanager.service.DataService;
import easyhattrickmanager.service.PlayerDataService;
import easyhattrickmanager.service.TeamTrainingService;
import easyhattrickmanager.service.UpdateService;
import easyhattrickmanager.service.model.dataresponse.CurrencyInfo;
import easyhattrickmanager.service.model.dataresponse.LanguageInfo;
import easyhattrickmanager.service.model.dataresponse.UserConfig;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingRequest;
import easyhattrickmanager.service.model.teamtraining.TeamTrainingResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("data")
public class DataController {

    private final DataService dataService;
    private final UpdateService updateService;
    private final PlayerDataService playerDataService;
    private TeamTrainingService teamTrainingService;

    @GetMapping
    public ResponseEntity<DataResponse> getData() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        DataResponse dataResponse = dataService.getData(username);
        return ResponseEntity.ok(dataResponse);
    }

    @GetMapping("languages")
    public ResponseEntity<List<LanguageInfo>> getLanguages() {
        List<LanguageInfo> languages = dataService.getLanguages();
        return ResponseEntity.ok(languages);
    }

    @GetMapping("currencies")
    public ResponseEntity<List<CurrencyInfo>> getCurrencies() {
        List<CurrencyInfo> currencies = dataService.getCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @PostMapping("user-config")
    public ResponseEntity<Void> saveUserConfig(@RequestBody UserConfig userConfig) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        dataService.saveUserConfig(username, userConfig);
        return ResponseEntity.ok().build();
    }

    @GetMapping("update")
    public ResponseEntity<Void> update() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        updateService.update(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("player/{playerId}")
    public ResponseEntity<PlayerDataResponse> getPlayerData(@PathVariable("playerId") int playerId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PlayerDataResponse playerDataResponse = playerDataService.getPlayerData(username, playerId);
        return ResponseEntity.ok(playerDataResponse);
    }

    @PostMapping("/teamTraining")
    public TeamTrainingResponse teamTraining(@RequestBody TeamTrainingRequest teamTrainingRequest) {
        return this.teamTrainingService.getTeamTraining(teamTrainingRequest);
    }
}
