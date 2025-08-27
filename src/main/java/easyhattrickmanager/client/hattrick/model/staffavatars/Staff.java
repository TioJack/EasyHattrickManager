package easyhattrickmanager.client.hattrick.model.staffavatars;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import easyhattrickmanager.client.hattrick.model.avatars.Avatar;
import lombok.Data;

@Data
public class Staff {

    @JacksonXmlProperty(localName = "StaffId")
    private int staffId;

    @JacksonXmlProperty(localName = "Avatar")
    private Avatar avatar;

}

