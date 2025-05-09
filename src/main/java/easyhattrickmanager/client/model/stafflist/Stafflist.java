package easyhattrickmanager.client.model.stafflist;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Stafflist {

    @JacksonXmlProperty(localName = "FileName")
    private String fileName;

    @JacksonXmlProperty(localName = "Version")
    private float version;

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "FetchedDate")
    private LocalDateTime fetchedDate;

    @JacksonXmlProperty(localName = "StaffList")
    private StaffList staffList;

}

