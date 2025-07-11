package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class YouthTeam {

    @JacksonXmlProperty(localName = "YouthTeamId")
    private int youthTeamId;

    @JacksonXmlProperty(localName = "YouthTeamName")
    private String youthTeamName;

    @JacksonXmlProperty(localName = "YouthLeague")
    private YouthLeague youthLeague;

}

