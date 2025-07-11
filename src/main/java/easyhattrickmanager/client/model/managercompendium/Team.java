package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Team {

    @JacksonXmlProperty(localName = "TeamId")
    private int teamId;

    @JacksonXmlProperty(localName = "TeamName")
    private String teamName;

    @JacksonXmlProperty(localName = "Arena")
    private Arena arena;

    @JacksonXmlProperty(localName = "League")
    private League league;

    @JacksonXmlProperty(localName = "Country")
    private Country country;

    @JacksonXmlProperty(localName = "LeagueLevelUnit")
    private LeagueLevelUnit leagueLevelUnit;

    @JacksonXmlProperty(localName = "Region")
    private Region region;

    @JacksonXmlProperty(localName = "YouthTeam")
    private YouthTeam youthTeam;

}

