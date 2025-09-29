package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class MatchOfficials {

    @JacksonXmlProperty(localName = "Referee")
    private Referee referee;

    @JacksonXmlProperty(localName = "RefereeAssistant1")
    private RefereeAssistant1 refereeAssistant1;

    @JacksonXmlProperty(localName = "RefereeAssistant2")
    private RefereeAssistant2 refereeAssistant2;

}

