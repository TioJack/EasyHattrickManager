package easyhattrickmanager.client.hattrick.model.playerdetails33;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class OwningTeam {

    @JacksonXmlProperty(localName = "TeamID")
    private int teamId;

    @JacksonXmlProperty(localName = "TeamName")
    private String teamName;

    @JacksonXmlProperty(localName = "LeagueID")
    private int leagueId;

}

