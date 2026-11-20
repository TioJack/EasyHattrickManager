package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Injury {

    @JacksonXmlProperty(localName = "InjuryPlayerID")
    private int playerId;
    @JacksonXmlProperty(localName = "InjuryType")
    private int type;
    @JacksonXmlProperty(localName = "InjuryMinute")
    private int minute;
}
