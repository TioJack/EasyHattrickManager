package easyhattrickmanager.client.model.avatars;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Layer {

    @JacksonXmlProperty(isAttribute = true)
    private int x;

    @JacksonXmlProperty(isAttribute = true)
    private int y;

    @JacksonXmlProperty(localName = "Image")
    private String image;

}
