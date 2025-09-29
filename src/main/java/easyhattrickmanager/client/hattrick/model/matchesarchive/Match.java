package easyhattrickmanager.client.hattrick.model.matchesarchive;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Match {

    @JacksonXmlProperty(localName = "MatchID")
    private int matchId;

    @JacksonXmlProperty(localName = "HomeTeam")
    private HomeTeam homeTeam;

    @JacksonXmlProperty(localName = "AwayTeam")
    private AwayTeam awayTeam;

    @JacksonXmlProperty(localName = "MatchDate")
    private ZonedDateTime matchDate;

    @JacksonXmlProperty(localName = "MatchType")
    private int matchType;

    @JacksonXmlProperty(localName = "MatchContextId")
    private int matchContextId;

    @JacksonXmlProperty(localName = "SourceSystem")
    private String sourceSystem;

    @JacksonXmlProperty(localName = "MatchRuleId")
    private int matchRuleId;

    @JacksonXmlProperty(localName = "CupId")
    private int cupId;

    @JacksonXmlProperty(localName = "CupLevel")
    private int cupLevel;

    @JacksonXmlProperty(localName = "CupLevelIndex")
    private int cupLevelIndex;

    @JacksonXmlProperty(localName = "HomeGoals")
    private int homeGoals;

    @JacksonXmlProperty(localName = "AwayGoals")
    private int awayGoals;

}

