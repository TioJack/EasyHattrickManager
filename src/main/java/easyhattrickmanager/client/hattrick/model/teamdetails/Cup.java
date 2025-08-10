package easyhattrickmanager.client.hattrick.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Cup {

    @JacksonXmlProperty(localName = "StillInCup")
    private boolean stillInCup;

}
