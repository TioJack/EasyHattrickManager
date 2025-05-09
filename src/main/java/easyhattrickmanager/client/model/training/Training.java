package easyhattrickmanager.client.model.training;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Training {

    @JacksonXmlProperty(localName = "FileName")
    private String fileName;

    @JacksonXmlProperty(localName = "Version")
    private float version;

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "FetchedDate")
    private LocalDateTime fetchedDate;

    @JacksonXmlProperty(localName = "UserSupporterTier")
    private String userSupporterTier;

    @JacksonXmlProperty(localName = "Team")
    private Team team;

}

