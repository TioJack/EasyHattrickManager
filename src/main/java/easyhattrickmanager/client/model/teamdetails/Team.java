package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;

@Data
public class Team {

    @JacksonXmlProperty(localName = "TeamID")
    private int teamId;

    @JacksonXmlProperty(localName = "TeamName")
    private String teamName;

    @JacksonXmlProperty(localName = "ShortTeamName")
    private String shortTeamName;

    @JacksonXmlProperty(localName = "IsPrimaryClub")
    private boolean primaryClub;

    @JacksonXmlProperty(localName = "FoundedDate")
    private ZonedDateTime foundedDate;

    @JacksonXmlProperty(localName = "Arena")
    private Arena arena;

    @JacksonXmlProperty(localName = "League")
    private League league;

    @JacksonXmlProperty(localName = "Country")
    private Country country;

    @JacksonXmlProperty(localName = "Region")
    private Region region;

    @JacksonXmlProperty(localName = "Trainer")
    private Trainer trainer;

    @JacksonXmlProperty(localName = "HomePage")
    private String homePage;

    @JacksonXmlProperty(localName = "DressURI")
    private String dressURI;

    @JacksonXmlProperty(localName = "DressAlternateURI")
    private String dressAlternateURI;

    @JacksonXmlProperty(localName = "LeagueLevelUnit")
    private LeagueLevelUnit leagueLevelUnit;

    @JacksonXmlProperty(localName = "BotStatus")
    private BotStatus botStatus;

    @JacksonXmlProperty(localName = "Cup")
    private Cup cup;

    @JacksonXmlProperty(localName = "PowerRating")
    private PowerRating powerRating;

    @JacksonXmlProperty(localName = "FriendlyTeamID")
    private int friendlyTeamId;

    @JacksonXmlProperty(localName = "NumberOfVictories")
    private int numberOfVictories;

    @JacksonXmlProperty(localName = "NumberOfUndefeated")
    private int numberOfUndefeated;

    @JacksonXmlProperty(localName = "TeamRank")
    private int teamRank;

    @JacksonXmlProperty(localName = "Fanclub")
    private Fanclub fanclub;

    @JacksonXmlProperty(localName = "LogoURL")
    private String logoURL;

    @JacksonXmlProperty(localName = "Guestbook")
    private Guestbook guestbook;

    @JacksonXmlProperty(localName = "PressAnnouncement")
    private PressAnnouncement pressAnnouncement;

    @JacksonXmlProperty(localName = "TeamColors")
    private String teamColors;

    @JacksonXmlProperty(localName = "YouthTeamID")
    private int youthTeamId;

    @JacksonXmlProperty(localName = "YouthTeamName")
    private String youthTeamName;

    @JacksonXmlProperty(localName = "NumberOfVisits")
    private int numberOfVisits;

    @JacksonXmlProperty(localName = "TrophyList")
    private List<Trophy> trophies;

    @JacksonXmlProperty(localName = "PossibleToChallengeMidweek")
    private boolean possibleToChallengeMidweek;

    @JacksonXmlProperty(localName = "PossibleToChallengeWeekend")
    private boolean possibleToChallengeWeekend;

}
