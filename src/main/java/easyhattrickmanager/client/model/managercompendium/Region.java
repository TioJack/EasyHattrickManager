package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Region {

    @JacksonXmlProperty(localName = "RegionId")
    private int regionId;

    @JacksonXmlProperty(localName = "RegionName")
    private String regionName;

}

