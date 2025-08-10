package easyhattrickmanager.client.hattrick.model.translations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class League {

    @JacksonXmlProperty(localName = "LeagueId")
    private int leagueId;

    @JacksonXmlProperty(localName = "LocalLeagueName")
    private String localLeagueName;

    @JacksonXmlProperty(localName = "LanguageLeagueName")
    private String languageLeagueName;

}

