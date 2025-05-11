package easyhattrickmanager.client.model.players;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Player {

    @JacksonXmlProperty(localName = "PlayerID")
    private int playerId;

    @JacksonXmlProperty(localName = "FirstName")
    private String firstName;

    @JacksonXmlProperty(localName = "NickName")
    private String nickName;

    @JacksonXmlProperty(localName = "LastName")
    private String lastName;

    @JacksonXmlProperty(localName = "PlayerNumber")
    private int playerNumber;

    @JacksonXmlProperty(localName = "Age")
    private int age;

    @JacksonXmlProperty(localName = "AgeDays")
    private int ageDays;

    @JacksonXmlProperty(localName = "ArrivalDate")
    private ZonedDateTime arrivalDate;

    @JacksonXmlProperty(localName = "OwnerNotes")
    private String ownerNotes;

    @JacksonXmlProperty(localName = "TSI")
    private int TSI;

    @JacksonXmlProperty(localName = "PlayerForm")
    private int playerForm;

    @JacksonXmlProperty(localName = "Statement")
    private String statement;

    @JacksonXmlProperty(localName = "Experience")
    private int experience;

    @JacksonXmlProperty(localName = "Loyalty")
    private int loyalty;

    @JacksonXmlProperty(localName = "MotherClubBonus")
    private boolean motherClubBonus;

    @JacksonXmlProperty(localName = "Leadership")
    private int leadership;

    @JacksonXmlProperty(localName = "Salary")
    private int salary;

    @JacksonXmlProperty(localName = "IsAbroad")
    private int abroad;

    @JacksonXmlProperty(localName = "Agreeability")
    private int agreeability;

    @JacksonXmlProperty(localName = "Aggressiveness")
    private int aggressiveness;

    @JacksonXmlProperty(localName = "Honesty")
    private int honesty;

    @JacksonXmlProperty(localName = "LeagueGoals")
    private int leagueGoals;

    @JacksonXmlProperty(localName = "CupGoals")
    private int cupGoals;

    @JacksonXmlProperty(localName = "FriendliesGoals")
    private int friendliesGoals;

    @JacksonXmlProperty(localName = "CareerGoals")
    private int careerGoals;

    @JacksonXmlProperty(localName = "CareerHattricks")
    private int careerHattricks;

    @JacksonXmlProperty(localName = "MatchesCurrentTeam")
    private int matchesCurrentTeam;

    @JacksonXmlProperty(localName = "GoalsCurrentTeam")
    private int goalsCurrentTeam;

    @JacksonXmlProperty(localName = "AssistsCurrentTeam")
    private int assistsCurrentTeam;

    @JacksonXmlProperty(localName = "CareerAssists")
    private int careerAssists;

    @JacksonXmlProperty(localName = "Specialty")
    private int specialty;

    @JacksonXmlProperty(localName = "TransferListed")
    private boolean transferListed;

    @JacksonXmlProperty(localName = "NationalTeamID")
    private int nationalTeamId;

    @JacksonXmlProperty(localName = "CountryID")
    private int countryId;

    @JacksonXmlProperty(localName = "Caps")
    private int caps;

    @JacksonXmlProperty(localName = "CapsU20")
    private int capsU20;

    @JacksonXmlProperty(localName = "Cards")
    private int cards;

    @JacksonXmlProperty(localName = "InjuryLevel")
    private float injuryLevel;

    @JacksonXmlProperty(localName = "StaminaSkill")
    private int staminaSkill;

    @JacksonXmlProperty(localName = "KeeperSkill")
    private int keeperSkill;

    @JacksonXmlProperty(localName = "PlaymakerSkill")
    private int playmakerSkill;

    @JacksonXmlProperty(localName = "ScorerSkill")
    private int scorerSkill;

    @JacksonXmlProperty(localName = "PassingSkill")
    private int passingSkill;

    @JacksonXmlProperty(localName = "WingerSkill")
    private int wingerSkill;

    @JacksonXmlProperty(localName = "DefenderSkill")
    private int defenderSkill;

    @JacksonXmlProperty(localName = "SetPiecesSkill")
    private int setPiecesSkill;

    @JacksonXmlProperty(localName = "PlayerCategoryId")
    private int playerCategoryId;

}

