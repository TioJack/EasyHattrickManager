package easyhattrickmanager.service;

import static easyhattrickmanager.utils.SeasonWeekUtils.convertToSeasonWeek;
import static java.time.ZonedDateTime.now;

import easyhattrickmanager.configuration.EhmConfiguration;
import easyhattrickmanager.repository.CheckDataDAO;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckDataService {

    private final CheckDataDAO checkDataDAO;
    private final EmailService emailService;
    private final EhmConfiguration ehmConfiguration;
    private final UpdateService updateService;

    @Scheduled(cron = "#{ehmConfiguration.cronCheckData}")
    public void checkData() throws Exception {
        try {
            String seasonWeek = convertToSeasonWeek(now());
            List<Integer> missTraining = checkDataDAO.checkTraining(seasonWeek);
            List<Integer> missPlayerData = checkDataDAO.checkPlayerData(seasonWeek);
            List<Integer> missTrainer = checkDataDAO.checkTrainer(seasonWeek);
            boolean res = missTraining.isEmpty()
                && missPlayerData.isEmpty()
                && missTrainer.isEmpty()
                && checkDataDAO.checkStartProjects();
            if (res) {
                System.out.println("Check data OK");
                emailService.send("EHM check data", "<strong><span style='color:#2e7d32;'>" + seasonWeek + " OK</span></strong>");
            } else {
                Stream.of(missTraining, missPlayerData, missTrainer)
                    .flatMap(List::stream)
                    .distinct()
                    .forEach(updateService::update);
                boolean res2 = checkDataDAO.checkTraining(seasonWeek).isEmpty()
                    && checkDataDAO.checkPlayerData(seasonWeek).isEmpty()
                    && checkDataDAO.checkTrainer(seasonWeek).isEmpty()
                    && checkDataDAO.checkStartProjects();
                if (res2) {
                    System.out.println("Check data OK");
                    emailService.send("EHM check data", "<strong><span style='color:#2e7d32;'>" + seasonWeek + " OK</span></strong>");
                } else {
                    System.err.println("Check data KO");
                    emailService.send("EHM check data", "<strong><span style='color:#c62828;'>" + seasonWeek + " KO</span></strong>");
                }
            }
        } catch (Exception exception) {
            System.err.println("Check data KO");
            emailService.send("EHM check data", "<strong><span style='color:#c62828;'>KO</span></strong>");
            throw exception;
        }
    }

}
