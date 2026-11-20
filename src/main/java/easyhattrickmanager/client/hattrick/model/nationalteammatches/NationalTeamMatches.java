package easyhattrickmanager.client.hattrick.model.nationalteammatches;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.List;
import lombok.Data;

@Data
public class NationalTeamMatches {

    @JacksonXmlElementWrapper(localName = "Matches")
    @JacksonXmlProperty(localName = "Match")
    private List<NationalTeamMatch> matches;

}
