package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;

@Data
public class Match {

    @JacksonXmlProperty(localName = "MatchID")
    private int matchId;

    @JacksonXmlProperty(localName = "MatchType")
    private int matchType;

    @JacksonXmlProperty(localName = "MatchContextId")
    private int matchContextId;

    @JacksonXmlProperty(localName = "MatchRuleId")
    private int matchRuleId;

    @JacksonXmlProperty(localName = "CupLevel")
    private int cupLevel;

    @JacksonXmlProperty(localName = "CupLevelIndex")
    private int cupLevelIndex;

    @JacksonXmlProperty(localName = "MatchDate")
    private ZonedDateTime matchDate;

    @JacksonXmlProperty(localName = "FinishedDate")
    private ZonedDateTime finishedDate;

    @JacksonXmlProperty(localName = "AddedMinutes")
    private int addedMinutes;

    @JacksonXmlProperty(localName = "HomeTeam")
    private HomeTeam homeTeam;

    @JacksonXmlProperty(localName = "AwayTeam")
    private AwayTeam awayTeam;

    @JacksonXmlProperty(localName = "Arena")
    private Arena arena;

    @JacksonXmlProperty(localName = "MatchOfficials")
    private MatchOfficials matchOfficials;

    @JacksonXmlProperty(localName = "Scorers")
    private List<Goal> goals;

    @JacksonXmlProperty(localName = "Bookings")
    private String bookings;

    @JacksonXmlProperty(localName = "Injuries")
    private String injuries;

    @JacksonXmlProperty(localName = "PossessionFirstHalfHome")
    private int possessionFirstHalfHome;

    @JacksonXmlProperty(localName = "PossessionFirstHalfAway")
    private int possessionFirstHalfAway;

    @JacksonXmlProperty(localName = "PossessionSecondHalfHome")
    private int possessionSecondHalfHome;

    @JacksonXmlProperty(localName = "PossessionSecondHalfAway")
    private int possessionSecondHalfAway;

}

