package easyhattrickmanager.client.hattrick.model.tournamentfixtures;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class TournamentFixtures {

    @JacksonXmlElementWrapper(localName = "Matches")
    @JacksonXmlProperty(localName = "Match")
    private List<TournamentMatch> matches;
}
