package easyhattrickmanager.client.hattrick.model.matchesarchive;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class Team {

    @JacksonXmlProperty(localName = "TeamID")
    private int teamId;

    @JacksonXmlProperty(localName = "TeamName")
    private String teamName;

    @JacksonXmlProperty(localName = "MatchList")
    private List<Match> matches;

}

