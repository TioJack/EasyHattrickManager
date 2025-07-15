package easyhattrickmanager.client.model.avatars;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Player {

    @JacksonXmlProperty(localName = "PlayerID")
    private int playerId;

    @JacksonXmlProperty(localName = "Avatar")
    private Avatar avatar;

}
