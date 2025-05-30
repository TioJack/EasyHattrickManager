package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Language {

    @JacksonXmlProperty(localName = "LanguageID")
    private int languageId;

    @JacksonXmlProperty(localName = "LanguageName")
    private String languageName;

}
