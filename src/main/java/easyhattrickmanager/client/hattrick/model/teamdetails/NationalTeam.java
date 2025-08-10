package easyhattrickmanager.client.hattrick.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class NationalTeam {

    @JacksonXmlProperty(localName = "Index")
    private int index;

    @JacksonXmlProperty(localName = "NationalTeamStaffType")
    private int nationalTeamStaffType;

    @JacksonXmlProperty(localName = "NationalTeamID")
    private int nationalTeamId;

    @JacksonXmlProperty(localName = "NationalTeamName")
    private String nationalTeamName;

}
