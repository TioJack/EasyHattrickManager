package easyhattrickmanager.client.hattrick.model.matchlineup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class AwayTeam {

    @JacksonXmlProperty(localName = "AwayTeamID")
    private int awayTeamId;

    @JacksonXmlProperty(localName = "AwayTeamName")
    private String awayTeamName;

}

