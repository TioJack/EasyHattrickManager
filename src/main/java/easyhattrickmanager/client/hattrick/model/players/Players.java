package easyhattrickmanager.client.hattrick.model.players;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Players {

    @JacksonXmlProperty(localName = "FileName")
    private String fileName;

    @JacksonXmlProperty(localName = "Version")
    private float version;

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "FetchedDate")
    private ZonedDateTime fetchedDate;

    @JacksonXmlProperty(localName = "UserSupporterTier")
    private String userSupporterTier;

    @JacksonXmlProperty(localName = "IsYouth")
    private boolean youth;

    @JacksonXmlProperty(localName = "ActionType")
    private String actionType;

    @JacksonXmlProperty(localName = "IsPlayingMatch")
    private boolean playingMatch;

    @JacksonXmlProperty(localName = "Team")
    private Team team;

}

