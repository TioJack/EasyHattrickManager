package easyhattrickmanager.client.model.stafflist;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Trainer {

    @JacksonXmlProperty(localName = "TrainerId")
    private int trainerId;

    @JacksonXmlProperty(localName = "Name")
    private String name;

    @JacksonXmlProperty(localName = "Age")
    private int age;

    @JacksonXmlProperty(localName = "AgeDays")
    private int ageDays;

    @JacksonXmlProperty(localName = "ContractDate")
    private ZonedDateTime contractDate;

    @JacksonXmlProperty(localName = "Cost")
    private int cost;

    @JacksonXmlProperty(localName = "CountryID")
    private int countryId;

    @JacksonXmlProperty(localName = "TrainerType")
    private int trainerType;

    @JacksonXmlProperty(localName = "Leadership")
    private int leadership;

    @JacksonXmlProperty(localName = "TrainerSkillLevel")
    private int trainerSkillLevel;

    @JacksonXmlProperty(localName = "TrainerStatus")
    private int trainerStatus;

}

