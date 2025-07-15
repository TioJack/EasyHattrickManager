package easyhattrickmanager.client.model.avatars;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class Avatar {

    @JacksonXmlProperty(localName = "BackgroundImage")
    private String backgroundImage;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Layer")
    private List<Layer> layers;

}
