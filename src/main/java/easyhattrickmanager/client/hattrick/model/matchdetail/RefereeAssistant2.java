package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class RefereeAssistant2 {

    @JacksonXmlProperty(localName = "RefereeId")
    private int refereeId;

    @JacksonXmlProperty(localName = "RefereeName")
    private String refereeName;

    @JacksonXmlProperty(localName = "RefereeCountryId")
    private int refereeCountryId;

    @JacksonXmlProperty(localName = "RefereeCountryName")
    private String refereeCountryName;

    @JacksonXmlProperty(localName = "RefereeTeamId")
    private int refereeTeamId;

    @JacksonXmlProperty(localName = "RefereeTeamname")
    private String refereeTeamname;

}
