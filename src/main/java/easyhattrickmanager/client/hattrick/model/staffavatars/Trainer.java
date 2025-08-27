package easyhattrickmanager.client.hattrick.model.staffavatars;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import easyhattrickmanager.client.hattrick.model.avatars.Avatar;
import lombok.Data;

@Data
public class Trainer {

    @JacksonXmlProperty(localName = "TrainerId")
    private int trainerId;

    @JacksonXmlProperty(localName = "Avatar")
    private Avatar avatar;

}

