package easyhattrickmanager.client.hattrick.model.translations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

@Data
public class Level {

    @JacksonXmlProperty(localName = "Value", isAttribute = true)
    private int value;

    @JacksonXmlText
    private String text;

}

