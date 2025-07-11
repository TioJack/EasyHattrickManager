package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class YouthLeague {

    @JacksonXmlProperty(localName = "YouthLeagueId")
    private int youthLeagueId;

    @JacksonXmlProperty(localName = "YouthLeagueName")
    private String youthLeagueName;

}

