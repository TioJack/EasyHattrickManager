package easyhattrickmanager.client.hattrick.model.playerdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class MotherClub {

    @JacksonXmlProperty(localName = "TeamID")
    private int teamId;

    @JacksonXmlProperty(localName = "TeamName")
    private String teamName;

}

