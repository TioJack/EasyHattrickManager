package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Avatar {

    @JacksonXmlProperty(localName = "BackgroundImage")
    private String backgroundImage;

    @JacksonXmlProperty(localName = "Layer")
    private String layer;

}

