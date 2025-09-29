package easyhattrickmanager.client.hattrick.model.matchlineup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class MatchLineup {

    @JacksonXmlProperty(localName = "FileName")
    private String fileName;

    @JacksonXmlProperty(localName = "Version")
    private float version;

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "FetchedDate")
    private ZonedDateTime fetchedDate;

    @JacksonXmlProperty(localName = "MatchID")
    private int matchId;

    @JacksonXmlProperty(localName = "SourceSystem")
    private String sourceSystem;

    @JacksonXmlProperty(localName = "MatchType")
    private int matchType;

    @JacksonXmlProperty(localName = "MatchContextId")
    private int matchContextId;

    @JacksonXmlProperty(localName = "MatchDate")
    private ZonedDateTime matchDate;

    @JacksonXmlProperty(localName = "HomeTeam")
    private HomeTeam homeTeam;

    @JacksonXmlProperty(localName = "AwayTeam")
    private AwayTeam awayTeam;

    @JacksonXmlProperty(localName = "Arena")
    private Arena arena;

    @JacksonXmlProperty(localName = "Team")
    private Team team;

}

