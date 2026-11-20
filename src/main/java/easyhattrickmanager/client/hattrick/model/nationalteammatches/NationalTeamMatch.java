package easyhattrickmanager.client.hattrick.model.nationalteammatches;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class NationalTeamMatch {

    @JacksonXmlProperty(localName = "MatchID")
    private int matchId;

    @JacksonXmlProperty(localName = "HomeTeamId")
    private int homeTeamId;

    @JacksonXmlProperty(localName = "AwayTeamId")
    private int awayTeamId;

    @JacksonXmlProperty(localName = "MatchType")
    private int matchType;

    @JacksonXmlProperty(localName = "MatchContextId")
    private int matchContextId;

    @JacksonXmlProperty(localName = "MatchDate")
    private ZonedDateTime matchDate;

}
