package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Trainer {

    @JacksonXmlProperty(localName = "PlayerID")
    private int playerId;

}
