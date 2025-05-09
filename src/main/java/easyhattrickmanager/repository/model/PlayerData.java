package easyhattrickmanager.repository.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerData {

    int id;
    String seasonWeek;
    LocalDateTime date;
    int teamId;
    String nickName;
    Integer playerNumber;
    int age;
    int ageDays;
    int TSI;
    int playerForm;
    int experience;
    int loyalty;
    boolean motherClubBonus;
    int leadership;
    int salary;
    float injuryLevel;
    int staminaSkill;
    int keeperSkill;
    int playmakerSkill;
    int scorerSkill;
    int passingSkill;
    int wingerSkill;
    int defenderSkill;
    int setPiecesSkill;
    int htms;
    int htms28;
}
