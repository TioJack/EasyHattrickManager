package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Fanclub {

    @JacksonXmlProperty(localName = "FanclubID")
    private int fanclubId;

    @JacksonXmlProperty(localName = "FanclubName")
    private String fanclubName;

    @JacksonXmlProperty(localName = "FanclubSize")
    private int fanclubSize;

}
