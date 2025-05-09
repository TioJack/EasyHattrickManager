package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class League {

    @JacksonXmlProperty(localName = "LeagueID")
    private int leagueId;

    @JacksonXmlProperty(localName = "LeagueName")
    private String leagueName;

}
