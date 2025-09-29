package easyhattrickmanager.client.hattrick.model.matchlineup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class HomeTeam {

    @JacksonXmlProperty(localName = "HomeTeamID")
    private int homeTeamId;

    @JacksonXmlProperty(localName = "HomeTeamName")
    private String homeTeamName;

}

