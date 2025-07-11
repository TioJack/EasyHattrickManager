package easyhattrickmanager.client.model.worldlanguages;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;

@Data
public class WorldLanguages {

    @JacksonXmlProperty(localName = "FileName")
    private String fileName;

    @JacksonXmlProperty(localName = "Version")
    private float version;

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "FetchedDate")
    private ZonedDateTime fetchedDate;

    @JacksonXmlProperty(localName = "LanguageList")
    private List<Language> languages;

}

