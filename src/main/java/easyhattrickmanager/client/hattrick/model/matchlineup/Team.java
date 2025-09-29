package easyhattrickmanager.client.hattrick.model.matchlineup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class Team {

    @JacksonXmlProperty(localName = "TeamID")
    private int teamId;

    @JacksonXmlProperty(localName = "TeamName")
    private String teamName;

    @JacksonXmlProperty(localName = "ExperienceLevel")
    private int experienceLevel;

    @JacksonXmlProperty(localName = "StyleOfPlay")
    private int styleOfPlay;

    @JacksonXmlProperty(localName = "StartingLineup")
    private List<Player> startingLineup;

    @JacksonXmlProperty(localName = "Substitutions")
    private List<Substitution> substitutions;

    @JacksonXmlProperty(localName = "Lineup")
    private List<Player> lineup;

}

