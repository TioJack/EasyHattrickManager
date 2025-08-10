package easyhattrickmanager.client.hattrick.model.translations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

@Data
public class Item {

    @JacksonXmlProperty(localName = "Value", isAttribute = true)
    private String value;

    @JacksonXmlText
    private String text;

}

