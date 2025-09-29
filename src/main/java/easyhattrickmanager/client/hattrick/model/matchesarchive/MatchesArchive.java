package easyhattrickmanager.client.hattrick.model.matchesarchive;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class MatchesArchive {

    @JacksonXmlProperty(localName = "FileName")
    private String fileName;

    @JacksonXmlProperty(localName = "Version")
    private float version;

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "FetchedDate")
    private ZonedDateTime fetchedDate;

    @JacksonXmlProperty(localName = "IsYouth")
    private boolean youth;

    @JacksonXmlProperty(localName = "Team")
    private Team team;

}

