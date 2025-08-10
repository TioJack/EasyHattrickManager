package easyhattrickmanager.client.hattrick.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class LeagueLevelUnit {

    @JacksonXmlProperty(localName = "LeagueLevelUnitID")
    private int leagueLevelUnitId;

    @JacksonXmlProperty(localName = "LeagueLevelUnitName")
    private String leagueLevelUnitName;

    @JacksonXmlProperty(localName = "LeagueLevel")
    private int leagueLevel;

}
