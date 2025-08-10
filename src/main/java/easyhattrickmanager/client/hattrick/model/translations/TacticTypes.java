package easyhattrickmanager.client.hattrick.model.translations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class TacticTypes {

    @JacksonXmlProperty(localName = "Label", isAttribute = true)
    private String label;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Item")
    private List<Item> items;

}

