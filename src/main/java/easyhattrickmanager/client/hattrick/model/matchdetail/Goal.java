package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Goal {

    @JacksonXmlProperty(localName = "Index")
    private int index;

    @JacksonXmlProperty(localName = "ScorerPlayerID")
    private int scorerPlayerId;

    @JacksonXmlProperty(localName = "ScorerPlayerName")
    private String scorerPlayerName;

    @JacksonXmlProperty(localName = "ScorerTeamID")
    private int scorerTeamId;

    @JacksonXmlProperty(localName = "ScorerHomeGoals")
    private int scorerHomeGoals;

    @JacksonXmlProperty(localName = "ScorerAwayGoals")
    private int scorerAwayGoals;

    @JacksonXmlProperty(localName = "ScorerMinute")
    private int scorerMinute;

    @JacksonXmlProperty(localName = "MatchPart")
    private int matchPart;

}

