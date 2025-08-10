package easyhattrickmanager.client.hattrick.model.avatars;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class Team {

    @JacksonXmlProperty(localName = "TeamId")
    private int teamId;

    @JacksonXmlProperty(localName = "Players")
    private List<Player> players;

}
