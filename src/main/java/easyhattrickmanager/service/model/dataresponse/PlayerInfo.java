package easyhattrickmanager.service.model.dataresponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerInfo {

    int id;
    String firstName;
    String nickName;
    String lastName;
    int agreeability;
    int aggressiveness;
    int honesty;
    int specialty;
    int countryId;
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
