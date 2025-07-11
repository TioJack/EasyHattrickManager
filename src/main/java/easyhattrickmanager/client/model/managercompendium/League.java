package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class League {

    @JacksonXmlProperty(localName = "LeagueId")
    private int leagueId;

    @JacksonXmlProperty(localName = "LeagueName")
    private String leagueName;

    @JacksonXmlProperty(localName = "Season")
    private int season;

}

