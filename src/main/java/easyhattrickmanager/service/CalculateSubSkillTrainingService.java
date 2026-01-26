package easyhattrickmanager.service;

import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.PlayerSubSkillDAO;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.PlayerSubSkill;
import easyhattrickmanager.repository.model.PlayerTraining;
import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.skilltrainingrequest.Skill;
import easyhattrickmanager.service.model.skilltrainingrequest.SkillTrainingRequest;
import easyhattrickmanager.utils.HTMSUtils;
import easyhattrickmanager.utils.SeasonWeekUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateSubSkillTrainingService {

    private final PlayerSubSkillDAO playerSubSkillDAO;
    private final PlayerDataDAO playerDataDAO;

    public List<PlayerSubSkill> calculateSubSkillTraining(String seasonWeek, int teamId, Training training, int trainerLevel, int assistantsLevel, List<PlayerData> playerDatas, List<PlayerTraining> playerTrainings) {
        if (playerDatas == null || playerDatas.isEmpty()) {
            return List.of();
        }

        List<PlayerTraining> safePlayerTrainings = playerTrainings == null ? List.of() : playerTrainings;
        String previousWeek = SeasonWeekUtils.previous(seasonWeek);
        Map<Integer, PlayerTraining> playerTrainingById = safePlayerTrainings.stream()
            .collect(Collectors.toMap(PlayerTraining::getId, Function.identity(), (a, b) -> a));
        Map<Integer, PlayerSubSkill> previousSubSkillById = playerSubSkillDAO.get(teamId).stream()
            .filter(playerSubSkill -> previousWeek.equals(playerSubSkill.getSeasonWeek()))
            .collect(Collectors.toMap(PlayerSubSkill::getId, Function.identity(), (a, b) -> a));
        Map<Integer, PlayerData> previousPlayerDataById = playerDataDAO.get(teamId).stream()
            .filter(playerData -> previousWeek.equals(playerData.getSeasonWeek()))
            .collect(Collectors.toMap(PlayerData::getId, Function.identity(), (a, b) -> a));

        List<PlayerSubSkill> result = new ArrayList<>();
        for (PlayerData playerData : playerDatas) {

            PlayerTraining playerTraining = playerTrainingById.get(playerData.getId());
            PlayerData previousPlayerData = previousPlayerDataById.getOrDefault(playerData.getId(), playerData);
            PlayerSubSkill previous = previousSubSkillById.get(playerData.getId());

            double previousStamina = previous == null ? 0.0 : previous.getStamina();
            double previousKeeper = previous == null ? 0.0 : previous.getKeeper();
            double previousDefender = previous == null ? 0.0 : previous.getDefender();
            double previousPlaymaker = previous == null ? 0.0 : previous.getPlaymaker();
            double previousWinger = previous == null ? 0.0 : previous.getWinger();
            double previousPassing = previous == null ? 0.0 : previous.getPassing();
            double previousScorer = previous == null ? 0.0 : previous.getScorer();
            double previousSetPieces = previous == null ? 0.0 : previous.getSetPieces();

            int minutesPlayed = playerTraining == null ? 0 : playerTraining.getMinutes();
            double keeperTraining = playerTraining == null ? 0.0 : playerTraining.getKeeper();
            double defenderTraining = playerTraining == null ? 0.0 : playerTraining.getDefender();
            double playmakerTraining = playerTraining == null ? 0.0 : playerTraining.getPlaymaker();
            double wingerTraining = playerTraining == null ? 0.0 : playerTraining.getWinger();
            double passingTraining = playerTraining == null ? 0.0 : playerTraining.getPassing();
            double scorerTraining = playerTraining == null ? 0.0 : playerTraining.getScorer();
            double setPiecesTraining = playerTraining == null ? 0.0 : playerTraining.getSetPieces();

            double stamina = applyStaminaTraining(previousPlayerData.getStaminaSkill(), previousStamina, playerData, training, minutesPlayed);
            double keeper = applyTraining(Skill.GOALKEEPING, keeperTraining, previousPlayerData.getKeeperSkill(), playerData.getKeeperSkill(), previousKeeper, playerData.getAge(), training, trainerLevel, assistantsLevel);
            double defender = applyTraining(Skill.DEFENDING, defenderTraining, previousPlayerData.getDefenderSkill(), playerData.getDefenderSkill(), previousDefender, playerData.getAge(), training, trainerLevel, assistantsLevel);
            double playmaker = applyTraining(Skill.PLAY_MAKING, playmakerTraining, previousPlayerData.getPlaymakerSkill(), playerData.getPlaymakerSkill(), previousPlaymaker, playerData.getAge(), training, trainerLevel, assistantsLevel);
            double winger = applyTraining(Skill.WINGER, wingerTraining, previousPlayerData.getWingerSkill(), playerData.getWingerSkill(), previousWinger, playerData.getAge(), training, trainerLevel, assistantsLevel);
            double passing = applyTraining(Skill.PASSING, passingTraining, previousPlayerData.getPassingSkill(), playerData.getPassingSkill(), previousPassing, playerData.getAge(), training, trainerLevel, assistantsLevel);
            double scorer = applyTraining(Skill.SCORING, scorerTraining, previousPlayerData.getScorerSkill(), playerData.getScorerSkill(), previousScorer, playerData.getAge(), training, trainerLevel, assistantsLevel);
            double setPieces = applyTraining(Skill.SET_PIECES, setPiecesTraining, previousPlayerData.getSetPiecesSkill(), playerData.getSetPiecesSkill(), previousSetPieces, playerData.getAge(), training, trainerLevel, assistantsLevel);

            HTMS htms = HTMSUtils.calculateHTMS(
                playerData.getAge(),
                playerData.getAgeDays(),
                playerData.getKeeperSkill() + keeper,
                playerData.getDefenderSkill() + defender,
                playerData.getPlaymakerSkill() + playmaker,
                playerData.getWingerSkill() + winger,
                playerData.getPassingSkill() + passing,
                playerData.getScorerSkill() + scorer,
                playerData.getSetPiecesSkill() + setPieces);

            result.add(PlayerSubSkill.builder()
                .id(playerData.getId())
                .seasonWeek(seasonWeek)
                .teamId(teamId)
                .stamina(stamina)
                .keeper(keeper)
                .defender(defender)
                .playmaker(playmaker)
                .winger(winger)
                .passing(passing)
                .scorer(scorer)
                .setPieces(setPieces)
                .htms(htms.getHtms())
                .htms28(htms.getHtms28())
                .build());
        }

        return result;
    }

    private double applyTraining(Skill skill, double trainingPercentage, int previousBaseSkill, int currentBaseSkill, double previousSubSkill, int age, Training training, int trainerLevel, int assistantsLevel) {
        double newSubSkill = previousSubSkill;
        if (trainingPercentage > 0) {
            double skillValue = previousBaseSkill + previousSubSkill;
            double baseTrainingPercentage = normalizeTrainingPercentage(trainingPercentage, training, trainerLevel, assistantsLevel);
            int minutes = (int) Math.round(baseTrainingPercentage * 90.0 / 100.0);
            SkillTrainingRequest rq = SkillTrainingRequest.builder()
                .skill(skillValue)
                .age(age)
                .coach(trainerLevel)
                .assistants(assistantsLevel)
                .intensity(training.getTrainingLevel())
                .stamina(training.getStaminaTrainingPart())
                .training(getTraining(skill, training.getTrainingType()))
                .minutes(minutes)
                .build();
            newSubSkill += getSkillTraining(rq);
        }
        newSubSkill -= getDropAge(skill, age);
        return normalizeSubSkill(previousBaseSkill, currentBaseSkill, newSubSkill);
    }

    private double normalizeSubSkill(int previousBaseSkill, int currentBaseSkill, double value) {
        if (previousBaseSkill < currentBaseSkill) {
            if (value < 1) {
                return 0;
            }
        }
        if (previousBaseSkill > currentBaseSkill) {
            if (value < 0) {
                return 1 + value;
            } else {
                return 0.99;
            }
        }
        if (value >= 1) {
            value -= 1;
            return value;
        }
        if (value < 0) {
            return 0;
        }
        return value;
    }

    private static final Map<Integer, Double> PER_TRAINER = Map.ofEntries(
        Map.entry(5, 1.0375 / 1.0375),
        Map.entry(4, 1.0000 / 1.0375),
        Map.entry(3, 0.9200 / 1.0375),
        Map.entry(2, 0.8324 / 1.0375),
        Map.entry(1, 0.7343 / 1.0375)
    );

    private static final Map<Integer, Double> PER_ASSISTANTS = Map.ofEntries(
        Map.entry(10, 1.350 / 1.350),
        Map.entry(9, 1.315 / 1.350),
        Map.entry(8, 1.280 / 1.350),
        Map.entry(7, 1.245 / 1.350),
        Map.entry(6, 1.210 / 1.350),
        Map.entry(5, 1.175 / 1.350),
        Map.entry(4, 1.140 / 1.350),
        Map.entry(3, 1.105 / 1.350),
        Map.entry(2, 1.070 / 1.350),
        Map.entry(1, 1.035 / 1.350),
        Map.entry(0, 1.000 / 1.350)
    );

    private double normalizeTrainingPercentage(double trainingPercentage, Training training, int trainerLevel, int assistantsLevel) {
        double trainingLevel = training.getTrainingLevel() / 100.0;
        double staminaPart = getStaminaTrainingPartCoefficient(training.getStaminaTrainingPart());
        double trainerPer = PER_TRAINER.getOrDefault(trainerLevel, 1.0);
        double assistantsPer = PER_ASSISTANTS.getOrDefault(assistantsLevel, 1.0);
        double normalization = trainingLevel * staminaPart * trainerPer * assistantsPer;
        if (normalization <= 0.0) {
            return trainingPercentage;
        }
        return trainingPercentage / normalization;
    }

    private double getStaminaTrainingPartCoefficient(int staminaTrainingPart) {
        return (100.0 - (((staminaTrainingPart - 10.0) * 100.0) / (100.0 - 10.0))) / 100.0;
    }

    private easyhattrickmanager.service.model.skilltrainingrequest.Training getTraining(Skill skill, int trainingType) {
        return switch (skill) {
            case GOALKEEPING -> easyhattrickmanager.service.model.skilltrainingrequest.Training.GOALKEEPING;
            case DEFENDING -> easyhattrickmanager.service.model.skilltrainingrequest.Training.DEFENDING;
            case PLAY_MAKING -> easyhattrickmanager.service.model.skilltrainingrequest.Training.PLAY_MAKING;
            case WINGER -> easyhattrickmanager.service.model.skilltrainingrequest.Training.WINGER;
            case PASSING -> easyhattrickmanager.service.model.skilltrainingrequest.Training.PASSING;
            case SCORING -> easyhattrickmanager.service.model.skilltrainingrequest.Training.SCORING;
            case SET_PIECES -> trainingType == 6
                ? easyhattrickmanager.service.model.skilltrainingrequest.Training.SET_PIECES_PLUS
                : easyhattrickmanager.service.model.skilltrainingrequest.Training.SET_PIECES;
        };
    }

    private double getSkillTraining(SkillTrainingRequest rq) {
        //T = f(lvl) * K(coach) * K(assist) * K(int) * K(stam) * K(train) * K(age) * K(time)
        final double training = Math.min(1, this.getSkill(rq.getSkill())
            * this.getCoefficientCoach(rq.getCoach())
            * this.getCoefficientAssistants(rq.getAssistants())
            * this.getCoefficientIntensity(rq.getIntensity())
            * this.getCoefficientStamina(rq.getStamina())
            * rq.getTraining().getCoefficient()
            * this.getCoefficientAge(rq.getAge())
            * this.getCoefficientTime(rq.getMinutes()));

        return Math.max(0, training - this.getDropLevel(rq.getSkill(), rq.getAge()));
    }

    private double getSkill(double skill) {
        final double lvl = skill - 1;
        return lvl < 9 ? 16.289 * Math.exp(-0.1396 * lvl) : 54.676 / lvl - 1.438;
    }

    private static final Map<Integer, Double> coefficientCoach = Map.ofEntries(
        Map.entry(1, 0.7343),
        Map.entry(2, 0.8324),
        Map.entry(3, 0.9200),
        Map.entry(4, 1.0000),
        Map.entry(5, 1.0375)
    );

    private double getCoefficientCoach(int coach) {
        return coefficientCoach.getOrDefault(coach, 0.0);
    }

    private static final Map<Integer, Double> coefficientAssistants = Map.ofEntries(
        Map.entry(0, 1.000),
        Map.entry(1, 1.035),
        Map.entry(2, 1.070),
        Map.entry(3, 1.105),
        Map.entry(4, 1.140),
        Map.entry(5, 1.175),
        Map.entry(6, 1.210),
        Map.entry(7, 1.245),
        Map.entry(8, 1.280),
        Map.entry(9, 1.315),
        Map.entry(10, 1.350)
    );

    private double getCoefficientAssistants(int assistants) {
        return coefficientAssistants.getOrDefault(assistants, 0.0);
    }

    private double getCoefficientIntensity(int intensity) {
        return intensity / 100.0;
    }

    private double getCoefficientStamina(int stamina) {
        return 1.0 - (stamina / 100.0);
    }

    private double getCoefficientAge(int age) {
        return 54.0 / (age + 37.0);
    }

    private double getCoefficientTime(int minutes) {
        return minutes / 90.0;
    }

    private static final double A = 0.000006111;
    private static final double B = 0.000808;
    private static final double C = -0.026017;
    private static final double D = 0.192775;
    private static final double E = 0.39;

    private static final Map<Integer, Double> M = Map.ofEntries(
        Map.entry(31, 0.00031),
        Map.entry(32, 0.00118),
        Map.entry(33, 0.00264),
        Map.entry(34, 0.00468),
        Map.entry(35, 0.00732),
        Map.entry(36, 0.01066),
        Map.entry(37, 0.01460)
    );

    private static final Map<Integer, Double> N = Map.ofEntries(
        Map.entry(31, -0.00434),
        Map.entry(32, -0.01625),
        Map.entry(33, -0.03551),
        Map.entry(34, -0.06086),
        Map.entry(35, -0.09104),
        Map.entry(36, -0.12554),
        Map.entry(37, -0.16021)
    );

    private double getDropLevel(double skill, final int age) {
        final double lvl = skill - 1;
        final double plus = lvl > 20 ? E : 0;
        final double dropLevel = 14 <= lvl ? A * Math.pow(lvl + plus, 3) + B * Math.pow(lvl + plus, 2) + C * (lvl + plus) + D : 0;
        final double factor = lvl > 20 ? lvl + 1 : lvl;
        final double dropLevelAge = age < 31 ? 0 : M.getOrDefault(age, 0.01460) * factor + N.getOrDefault(age, -0.16021);
        return dropLevel + dropLevelAge;
    }

    private static final Map<Integer, Double> AGE_LOSS = Map.ofEntries(
        Map.entry(1, 0.0003),
        Map.entry(2, 0.0014),
        Map.entry(3, 0.0037),
        Map.entry(4, 0.0074),
        Map.entry(5, 0.0127),
        Map.entry(6, 0.0197),
        Map.entry(7, 0.0285),
        Map.entry(8, 0.0393),
        Map.entry(9, 0.0522),
        Map.entry(10, 0.0673),
        Map.entry(11, 0.0846)
    );

    private double getDropAge(Skill skill, final int age) {
        final int ageLoss = age - skill.getAgeNoDrop();
        return ageLoss < 1 ? 0 : AGE_LOSS.getOrDefault(ageLoss, 0.0846);
    }

    private static final Map<Integer, Integer> K = Map.ofEntries(
        Map.entry(17, 3),
        Map.entry(18, 3),
        Map.entry(19, 3),
        Map.entry(20, 0),
        Map.entry(21, 0),
        Map.entry(22, 0),
        Map.entry(23, 0),
        Map.entry(24, 0),
        Map.entry(25, 1),
        Map.entry(26, 2),
        Map.entry(27, 3),
        Map.entry(28, 4),
        Map.entry(29, 5),
        Map.entry(30, 6),
        Map.entry(31, 7),
        Map.entry(32, 8),
        Map.entry(33, 9),
        Map.entry(34, 11),
        Map.entry(35, 13),
        Map.entry(36, 15),
        Map.entry(37, 17),
        Map.entry(38, 19),
        Map.entry(39, 21)
    );

    private static final Map<Integer, Integer> S_MAX = Map.ofEntries(
        Map.entry(17, 282),
        Map.entry(18, 282),
        Map.entry(19, 282),
        Map.entry(20, 282),
        Map.entry(21, 282),
        Map.entry(22, 282),
        Map.entry(23, 282),
        Map.entry(24, 282),
        Map.entry(25, 282),
        Map.entry(26, 282),
        Map.entry(27, 282),
        Map.entry(28, 275),
        Map.entry(29, 268),
        Map.entry(30, 261),
        Map.entry(31, 254),
        Map.entry(32, 247),
        Map.entry(33, 240),
        Map.entry(34, 226),
        Map.entry(35, 212),
        Map.entry(36, 198),
        Map.entry(37, 184),
        Map.entry(38, 170),
        Map.entry(39, 156)
    );

    private double applyStaminaTraining(int previousBaseStamina, double previousSubStamina, PlayerData playerData, Training training, int minutesPlayed) {
        double staminaValue = previousBaseStamina + previousSubStamina;
        double staminaTrainingEffect = calculateStaminaTrainingEffect(playerData, minutesPlayed);
        double newStamina = getStaminaTraining(playerData.getAge(), staminaValue, training.getStaminaTrainingPart(), training.getTrainingLevel(), staminaTrainingEffect);

        if (playerData.getStaminaSkill() == Math.floor(newStamina)) {
            newStamina = newStamina - Math.floor(newStamina);
        } else if (playerData.getStaminaSkill() == Math.floor(newStamina - 1)) {
            newStamina = 0.99;
        } else if (playerData.getStaminaSkill() == Math.floor(newStamina + 1)) {
            newStamina = 0;
        }

        return newStamina;
    }

    private double calculateStaminaTrainingEffect(PlayerData playerData, int minutesPlayed) {
        if (playerData.getInjuryLevel() > 0) {
            return 0.0;
        }
        if (minutesPlayed >= 90) {
            return 1.0;
        }
        if (minutesPlayed > 0) {
            return 0.75 + ((minutesPlayed / 90.0) * 0.25);
        }
        return 0.5;
    }

    private double getStaminaTraining(int playerAge, double playerStamina, int trainingStamina, int trainingIntensity, double staminaTrainingEffect) {
        double lvl = playerStamina - 1.0;
        double kage = K.getOrDefault(playerAge, 21) * 7.0 / 30.0;
        double l = lvl + kage;
        double s = (trainingStamina / 100.0) * (trainingIntensity / 100.0) * staminaTrainingEffect;

        double stamina = 0.0;
        if (l >= 7.56) {
            stamina = -1.05 * Math.pow(s, 2) + 2.1 * s + (-0.00016) * Math.pow(l, 3) + (-0.00544) * Math.pow(l, 2) + 0.0013 * l + (-0.0185);
        }
        if (7.0 < l && l < 7.56) {
            stamina = -1.05 * Math.pow(s, 2) + 2.1 * s + (-0.00772) * Math.pow(l, 3) + 0.0636 * Math.pow(l, 2) + (-0.0178) * l + (-0.554);
        }
        if (l <= 7.0) {
            stamina = (-1.05 * Math.pow(s, 2) + 2.1 * s) * (0.00013 * Math.pow(l, 3) + 0.0048 * Math.pow(l, 2) + (-0.301) * l + 2.826) - 0.21;
        }
        return Math.max(0.7, Math.min(S_MAX.getOrDefault(playerAge, 156) / 30.0, playerStamina + stamina));
    }

}
