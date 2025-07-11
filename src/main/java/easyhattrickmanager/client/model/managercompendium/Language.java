package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Language {

    @JacksonXmlProperty(localName = "LanguageId")
    private int languageId;

    @JacksonXmlProperty(localName = "LanguageName")
    private String languageName;

}

