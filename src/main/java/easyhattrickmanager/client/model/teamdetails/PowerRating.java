package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class PowerRating {

    @JacksonXmlProperty(localName = "GlobalRanking")
    private int globalRanking;

    @JacksonXmlProperty(localName = "LeagueRanking")
    private int leagueRanking;

    @JacksonXmlProperty(localName = "RegionRanking")
    private int regionRanking;

    @JacksonXmlProperty(localName = "PowerRating")
    private int powerRating;

}
