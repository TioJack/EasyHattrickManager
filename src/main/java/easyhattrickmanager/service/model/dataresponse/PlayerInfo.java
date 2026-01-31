package easyhattrickmanager.service.model.dataresponse;

import java.time.ZonedDateTime;
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
    ZonedDateTime arrivalDate;
    //String ownerNotes;
    int TSI;
    int playerForm;
    //String statement;
    int experience;
    int loyalty;
    boolean motherClubBonus;
    int leadership;
    int salary;
    boolean abroad;
    //int leagueGoals;
    //int cupGoals;
    //int friendliesGoals;
    //int careerGoals;
    //int careerHattricks;
    //int matchesCurrentTeam;
    //int goalsCurrentTeam;
    //int assistsCurrentTeam;
    //int careerAssists;
    boolean transferListed;
    //int nationalTeamId;
    //int caps;
    //int capsU21;
    int cards;
    int injuryLevel;
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
    int playerCategoryId;
    PlayerTrainingInfo playerTraining;
    PlayerSubSkillInfo playerSubSkill;
}
