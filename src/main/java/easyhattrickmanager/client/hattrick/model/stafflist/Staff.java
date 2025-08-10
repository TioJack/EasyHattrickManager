package easyhattrickmanager.client.hattrick.model.stafflist;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Staff {

    @JacksonXmlProperty(localName = "Name")
    private String name;

    @JacksonXmlProperty(localName = "StaffId")
    private int staffId;

    @JacksonXmlProperty(localName = "StaffType")
    private int staffType;

    @JacksonXmlProperty(localName = "StaffLevel")
    private int staffLevel;

    @JacksonXmlProperty(localName = "HiredDate")
    private ZonedDateTime hiredDate;

    @JacksonXmlProperty(localName = "Cost")
    private int cost;

    @JacksonXmlProperty(localName = "HofPlayerId")
    private int hofPlayerId;

}

