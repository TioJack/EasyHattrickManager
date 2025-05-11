package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Trophy {

    @JacksonXmlProperty(localName = "TrophyTypeId")
    private int trophyTypeId;

    @JacksonXmlProperty(localName = "TrophySeason")
    private int trophySeason;

    @JacksonXmlProperty(localName = "LeagueLevel")
    private int leagueLevel;

    @JacksonXmlProperty(localName = "LeagueLevelUnitId")
    private int leagueLevelUnitId;

    @JacksonXmlProperty(localName = "LeagueLevelUnitName")
    private String leagueLevelUnitName;

    @JacksonXmlProperty(localName = "GainedDate")
    private ZonedDateTime gainedDate;

    @JacksonXmlProperty(localName = "ImageUrl")
    private String imageUrl;

    @JacksonXmlProperty(localName = "CupLeagueLevel")
    private String cupLeagueLevel;

    @JacksonXmlProperty(localName = "CupLevel")
    private String cupLevel;

    @JacksonXmlProperty(localName = "CupLevelIndex")
    private String cupLevelIndex;

}
