package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class MatchDetail {

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

    @JacksonXmlProperty(localName = "SourceSystem")
    private String sourceSystem;

    @JacksonXmlProperty(localName = "Match")
    private Match match;

}

