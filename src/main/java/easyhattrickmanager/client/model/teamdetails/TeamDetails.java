package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class TeamDetails {

    @JacksonXmlProperty(localName = "FileName")
    private String fileName;

    @JacksonXmlProperty(localName = "Version")
    private float version;

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "FetchedDate")
    private LocalDateTime fetchedDate;

    @JacksonXmlProperty(localName = "User")
    private User user;

    @JacksonXmlProperty(localName = "Teams")
    private List<Team> teams;

}
