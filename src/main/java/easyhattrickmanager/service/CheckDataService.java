package easyhattrickmanager.service;

import static easyhattrickmanager.utils.SeasonWeekUtils.convertToSeasonWeek;
import static java.time.ZonedDateTime.now;

import easyhattrickmanager.configuration.EhmConfiguration;
import easyhattrickmanager.repository.CheckDataDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckDataService {

    private final CheckDataDAO checkDataDAO;
    private final EmailService emailService;
    private final EhmConfiguration ehmConfiguration;

    @Scheduled(cron = "#{ehmConfiguration.cronCheckData}")
    public void addActiveUpdateExecutions() throws Exception {
        try {
            String seasonWeek = convertToSeasonWeek(now());
            boolean res = checkDataDAO.checkTraining(seasonWeek)
                && checkDataDAO.checkPlayerData(seasonWeek)
                && checkDataDAO.checkTrainer(seasonWeek)
                && checkDataDAO.checkStartProjects();
            if (res) {
                System.out.println("Check data OK");
                emailService.send("EHM check data", "<strong><span style='color:#2e7d32;'>" + seasonWeek + " OK</span></strong>");
            } else {
                System.err.println("Check data KO");
                emailService.send("EHM check data", "<strong><span style='color:#c62828;'>" + seasonWeek + " KO</span></strong>");
            }
        } catch (Exception e) {
            System.err.println("Check data KO");
            emailService.send("EHM check data", "<strong><span style='color:#c62828;'>KO</span></strong>");
        }
    }

}
