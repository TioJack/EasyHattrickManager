package easyhattrickmanager.client.hattrick.model.tournamentfixtures;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class TournamentMatch {

    @JacksonXmlProperty(localName = "MatchId")
    private int matchId;
    @JacksonXmlProperty(localName = "HomeTeamId")
    private int homeTeamId;
    @JacksonXmlProperty(localName = "AwayTeamId")
    private int awayTeamId;
    @JacksonXmlProperty(localName = "MatchDate")
    private ZonedDateTime matchDate;
    @JacksonXmlProperty(localName = "MatchType")
    private int matchType;
    @JacksonXmlProperty(localName = "MatchRound")
    private int matchRound;
    @JacksonXmlProperty(localName = "Group")
    private int group;
}
