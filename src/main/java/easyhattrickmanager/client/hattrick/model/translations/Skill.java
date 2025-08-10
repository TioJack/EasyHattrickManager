package easyhattrickmanager.client.hattrick.model.translations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

@Data
public class Skill {

    @JacksonXmlProperty(localName = "Type", isAttribute = true)
    private String type;

    @JacksonXmlText
    private String text;

}

