package easyhattrickmanager.client.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PressAnnouncement {

    @JacksonXmlProperty(localName = "Subject")
    private String subject;

    @JacksonXmlProperty(localName = "Body")
    private String body;

    @JacksonXmlProperty(localName = "SendDate")
    private LocalDateTime sendDate;

}
