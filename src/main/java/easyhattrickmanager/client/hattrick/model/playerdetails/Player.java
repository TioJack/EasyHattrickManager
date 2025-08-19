package easyhattrickmanager.client.hattrick.model.playerdetails;

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

    @JacksonXmlProperty(localName = "NextBirthDay")
    private ZonedDateTime nextBirthDay;

    @JacksonXmlProperty(localName = "ArrivalDate")
    private ZonedDateTime arrivalDate;

    @JacksonXmlProperty(localName = "PlayerForm")
    private int playerForm;

    @JacksonXmlProperty(localName = "Cards")
    private int cards;

    @JacksonXmlProperty(localName = "InjuryLevel")
    private int injuryLevel;

    @JacksonXmlProperty(localName = "Statement")
    private String statement;

    @JacksonXmlProperty(localName = "PlayerLanguage")
    private String playerLanguage;

    @JacksonXmlProperty(localName = "PlayerLanguageID")
    private int playerLanguageId;

    @JacksonXmlProperty(localName = "TrainerData")
    private String trainerData;

    @JacksonXmlProperty(localName = "OwningTeam")
    private OwningTeam owningTeam;

    @JacksonXmlProperty(localName = "Salary")
    private int salary;

    @JacksonXmlProperty(localName = "IsAbroad")
    private boolean abroad;

    @JacksonXmlProperty(localName = "Agreeability")
    private int agreeability;

    @JacksonXmlProperty(localName = "Aggressiveness")
    private int aggressiveness;

    @JacksonXmlProperty(localName = "Honesty")
    private int honesty;

    @JacksonXmlProperty(localName = "Experience")
    private int experience;

    @JacksonXmlProperty(localName = "Loyalty")
    private int loyalty;

    @JacksonXmlProperty(localName = "MotherClubBonus")
    private boolean motherClubBonus;

    @JacksonXmlProperty(localName = "MotherClub")
    private MotherClub motherClub;

    @JacksonXmlProperty(localName = "Leadership")
    private int leadership;

    @JacksonXmlProperty(localName = "Specialty")
    private int specialty;

    @JacksonXmlProperty(localName = "NativeCountryID")
    private int nativeCountryId;

    @JacksonXmlProperty(localName = "NativeLeagueID")
    private int nativeLeagueId;

    @JacksonXmlProperty(localName = "NativeLeagueName")
    private String nativeLeagueName;

    @JacksonXmlProperty(localName = "TSI")
    private int tSI;

    @JacksonXmlProperty(localName = "PlayerSkills")
    private PlayerSkills playerSkills;

    @JacksonXmlProperty(localName = "Caps")
    private int caps;

    @JacksonXmlProperty(localName = "CapsU20")
    private int capsU20;

    @JacksonXmlProperty(localName = "CareerGoals")
    private int careerGoals;

    @JacksonXmlProperty(localName = "CareerHattricks")
    private int careerHattricks;

    @JacksonXmlProperty(localName = "LeagueGoals")
    private int leagueGoals;

    @JacksonXmlProperty(localName = "CupGoals")
    private int cupGoals;

    @JacksonXmlProperty(localName = "FriendliesGoals")
    private int friendliesGoals;

    @JacksonXmlProperty(localName = "MatchesCurrentTeam")
    private int matchesCurrentTeam;

    @JacksonXmlProperty(localName = "GoalsCurrentTeam")
    private int goalsCurrentTeam;

    @JacksonXmlProperty(localName = "AssistsCurrentTeam")
    private int assistsCurrentTeam;

    @JacksonXmlProperty(localName = "CareerAssists")
    private int careerAssists;

    @JacksonXmlProperty(localName = "TransferListed")
    private boolean transferListed;

    @JacksonXmlProperty(localName = "TransferDetails")
    private String transferDetails;

}

