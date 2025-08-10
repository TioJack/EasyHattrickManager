package easyhattrickmanager.client.hattrick.model.worlddetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;

@Data
public class League {

    @JacksonXmlProperty(localName = "LeagueID")
    private int leagueId;

    @JacksonXmlProperty(localName = "LeagueName")
    private String leagueName;

    @JacksonXmlProperty(localName = "Season")
    private int season;

    @JacksonXmlProperty(localName = "SeasonOffset")
    private int seasonOffset;

    @JacksonXmlProperty(localName = "MatchRound")
    private int matchRound;

    @JacksonXmlProperty(localName = "ShortName")
    private String shortName;

    @JacksonXmlProperty(localName = "Continent")
    private String continent;

    @JacksonXmlProperty(localName = "ZoneName")
    private String zoneName;

    @JacksonXmlProperty(localName = "EnglishName")
    private String englishName;

    @JacksonXmlProperty(localName = "LanguageId")
    private int languageId;

    @JacksonXmlProperty(localName = "LanguageName")
    private String languageName;

    @JacksonXmlProperty(localName = "Country")
    private Country country;

    @JacksonXmlProperty(localName = "Cups")
    private List<Cup> cups;

    @JacksonXmlProperty(localName = "NationalTeamId")
    private int nationalTeamId;

    @JacksonXmlProperty(localName = "U20TeamId")
    private int u20TeamId;

    @JacksonXmlProperty(localName = "ActiveTeams")
    private int activeTeams;

    @JacksonXmlProperty(localName = "ActiveUsers")
    private int activeUsers;

    @JacksonXmlProperty(localName = "WaitingUsers")
    private int waitingUsers;

    @JacksonXmlProperty(localName = "TrainingDate")
    private ZonedDateTime trainingDate;

    @JacksonXmlProperty(localName = "EconomyDate")
    private ZonedDateTime economyDate;

    @JacksonXmlProperty(localName = "CupMatchDate")
    private ZonedDateTime cupMatchDate;

    @JacksonXmlProperty(localName = "SeriesMatchDate")
    private ZonedDateTime seriesMatchDate;

    @JacksonXmlProperty(localName = "Sequence1")
    private ZonedDateTime sequence1;

    @JacksonXmlProperty(localName = "Sequence2")
    private ZonedDateTime sequence2;

    @JacksonXmlProperty(localName = "Sequence3")
    private ZonedDateTime sequence3;

    @JacksonXmlProperty(localName = "Sequence5")
    private ZonedDateTime sequence5;

    @JacksonXmlProperty(localName = "Sequence7")
    private ZonedDateTime sequence7;

    @JacksonXmlProperty(localName = "NumberOfLevels")
    private int numberOfLevels;

}

