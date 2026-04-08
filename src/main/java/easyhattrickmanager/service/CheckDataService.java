package easyhattrickmanager.service;

import static easyhattrickmanager.utils.SeasonWeekUtils.convertToSeasonWeek;
import static java.time.ZonedDateTime.now;

import easyhattrickmanager.configuration.EhmConfiguration;
import easyhattrickmanager.repository.CheckDataDAO;
import java.util.List;
import java.util.stream.Collectors;
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
            List<Integer> missStartProjects = checkDataDAO.checkStartProjects();
            boolean res = missTraining.isEmpty()
                && missPlayerData.isEmpty()
                && missTrainer.isEmpty()
                && missStartProjects.isEmpty();
            if (res) {
                System.out.println("Check data OK");
                emailService.send("EHM check data", "<strong><span style='color:#2e7d32;'>" + seasonWeek + " OK</span></strong>");
            } else {
                Stream.of(missTraining, missPlayerData, missTrainer, missStartProjects)
                    .flatMap(List::stream)
                    .distinct()
                    .forEach(updateService::update);
                missStartProjects.stream()
                    .distinct()
                    .forEach(updateService::completeStartProjects);
                List<Integer> missTraining2 = checkDataDAO.checkTraining(seasonWeek);
                List<Integer> missPlayerData2 = checkDataDAO.checkPlayerData(seasonWeek);
                List<Integer> missTrainer2 = checkDataDAO.checkTrainer(seasonWeek);
                List<Integer> missStartProjects2 = checkDataDAO.checkStartProjects();
                boolean res2 = missTraining2.isEmpty()
                    && missPlayerData2.isEmpty()
                    && missTrainer2.isEmpty()
                    && missStartProjects2.isEmpty();
                if (res2) {
                    System.out.println("Check data OK");
                    emailService.send("EHM check data", "<strong><span style='color:#2e7d32;'>" + seasonWeek + " OK</span></strong>");
                } else {
                    System.err.println("Check data KO");
                    String affectedTeams = Stream.of(missTraining2, missPlayerData2, missTrainer2, missStartProjects2)
                        .flatMap(List::stream)
                        .distinct()
                        .map(String::valueOf)
                        .collect(Collectors.joining("<br>"));
                    emailService.send("EHM check data", "<strong><span style='color:#c62828;'>" + seasonWeek + " KO</span></strong><br><br>" + "<strong>Affected teams:</strong><br>" + affectedTeams);
                }
            }
        } catch (Exception exception) {
            System.err.println("Check data KO");
            emailService.send("EHM check data", "<strong><span style='color:#c62828;'>KO</span></strong>");
            throw exception;
        }
    }

}
