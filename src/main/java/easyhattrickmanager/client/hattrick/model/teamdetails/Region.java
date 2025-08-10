package easyhattrickmanager.client.hattrick.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Region {

    @JacksonXmlProperty(localName = "RegionID")
    private int regionId;

    @JacksonXmlProperty(localName = "RegionName")
    private String regionName;

}
