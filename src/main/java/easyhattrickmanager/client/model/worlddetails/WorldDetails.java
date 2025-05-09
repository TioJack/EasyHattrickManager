package easyhattrickmanager.client.model.worlddetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class WorldDetails {

    @JacksonXmlProperty(localName = "FileName")
    private String fileName;

    @JacksonXmlProperty(localName = "Version")
    private float version;

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "FetchedDate")
    private LocalDateTime fetchedDate;

    @JacksonXmlProperty(localName = "LeagueList")
    private List<League> leagues;

}

