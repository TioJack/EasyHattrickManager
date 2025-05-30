package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Arena {

    @JacksonXmlProperty(localName = "ArenaID")
    private int arenaId;

    @JacksonXmlProperty(localName = "ArenaName")
    private String arenaName;

}
