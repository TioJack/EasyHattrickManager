package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Arena {

    @JacksonXmlProperty(localName = "ArenaID")
    private int arenaId;

    @JacksonXmlProperty(localName = "ArenaName")
    private String arenaName;

    @JacksonXmlProperty(localName = "WeatherID")
    private int weatherId;

    @JacksonXmlProperty(localName = "SoldTotal")
    private int soldTotal;

    @JacksonXmlProperty(localName = "SoldTerraces")
    private int soldTerraces;

    @JacksonXmlProperty(localName = "SoldBasic")
    private int soldBasic;

    @JacksonXmlProperty(localName = "SoldRoof")
    private int soldRoof;

    @JacksonXmlProperty(localName = "SoldVIP")
    private int soldVIP;

}

