package easyhattrickmanager.client.model.worlddetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Cup {

    @JacksonXmlProperty(localName = "CupID")
    private int cupId;

    @JacksonXmlProperty(localName = "CupName")
    private String cupName;

    @JacksonXmlProperty(localName = "CupLeagueLevel")
    private int cupLeagueLevel;

    @JacksonXmlProperty(localName = "CupLevel")
    private int cupLevel;

    @JacksonXmlProperty(localName = "CupLevelIndex")
    private int cupLevelIndex;

    @JacksonXmlProperty(localName = "MatchRound")
    private int matchRound;

    @JacksonXmlProperty(localName = "MatchRoundsLeft")
    private int matchRoundsLeft;

}

