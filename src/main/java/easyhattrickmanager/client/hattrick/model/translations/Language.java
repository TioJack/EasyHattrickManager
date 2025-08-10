package easyhattrickmanager.client.hattrick.model.translations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

@Data
public class Language {

    @JacksonXmlProperty(localName = "Id", isAttribute = true)
    private int id;

    @JacksonXmlText
    private String text;

}

