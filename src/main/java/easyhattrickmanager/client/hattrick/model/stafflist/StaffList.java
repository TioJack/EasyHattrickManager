package easyhattrickmanager.client.hattrick.model.stafflist;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class StaffList {

    @JacksonXmlProperty(localName = "Trainer")
    private Trainer trainer;

    @JacksonXmlProperty(localName = "StaffMembers")
    private List<Staff> staffs;

    @JacksonXmlProperty(localName = "TotalStaffMembers")
    private int totalStaffMembers;

    @JacksonXmlProperty(localName = "TotalCost")
    private int totalCost;

}

