package easyhattrickmanager.client.model.worlddetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
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
    private LocalDateTime trainingDate;

    @JacksonXmlProperty(localName = "EconomyDate")
    private LocalDateTime economyDate;

    @JacksonXmlProperty(localName = "CupMatchDate")
    private LocalDateTime cupMatchDate;

    @JacksonXmlProperty(localName = "SeriesMatchDate")
    private LocalDateTime seriesMatchDate;

    @JacksonXmlProperty(localName = "Sequence1")
    private LocalDateTime sequence1;

    @JacksonXmlProperty(localName = "Sequence2")
    private LocalDateTime sequence2;

    @JacksonXmlProperty(localName = "Sequence3")
    private LocalDateTime sequence3;

    @JacksonXmlProperty(localName = "Sequence5")
    private LocalDateTime sequence5;

    @JacksonXmlProperty(localName = "Sequence7")
    private LocalDateTime sequence7;

    @JacksonXmlProperty(localName = "NumberOfLevels")
    private int numberOfLevels;

}

