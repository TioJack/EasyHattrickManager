package easyhattrickmanager.client.hattrick.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Arena {

    @JacksonXmlProperty(localName = "ArenaId")
    private int arenaId;

    @JacksonXmlProperty(localName = "ArenaName")
    private String arenaName;

}

