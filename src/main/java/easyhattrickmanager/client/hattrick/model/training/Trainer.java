package easyhattrickmanager.client.hattrick.model.training;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Trainer {

    @JacksonXmlProperty(localName = "TrainerID")
    private int trainerId;

    @JacksonXmlProperty(localName = "TrainerName")
    private String trainerName;

    @JacksonXmlProperty(localName = "ArrivalDate")
    private ZonedDateTime arrivalDate;

}

