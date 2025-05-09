package easyhattrickmanager.client.model.training;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Trainer {

    @JacksonXmlProperty(localName = "TrainerID")
    private int trainerId;

    @JacksonXmlProperty(localName = "TrainerName")
    private String trainerName;

    @JacksonXmlProperty(localName = "ArrivalDate")
    private LocalDateTime arrivalDate;

}

