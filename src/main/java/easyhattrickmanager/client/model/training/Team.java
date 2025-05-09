package easyhattrickmanager.client.model.training;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Team {

    @JacksonXmlProperty(localName = "TeamID")
    private int teamId;

    @JacksonXmlProperty(localName = "TeamName")
    private String teamName;

    @JacksonXmlProperty(localName = "TrainingLevel")
    private int trainingLevel;

    @JacksonXmlProperty(localName = "NewTrainingLevel")
    private int newTrainingLevel;

    @JacksonXmlProperty(localName = "TrainingType")
    private int trainingType;

    @JacksonXmlProperty(localName = "StaminaTrainingPart")
    private int staminaTrainingPart;

    @JacksonXmlProperty(localName = "LastTrainingTrainingType")
    private int lastTrainingTrainingType;

    @JacksonXmlProperty(localName = "LastTrainingTrainingLevel")
    private int lastTrainingTrainingLevel;

    @JacksonXmlProperty(localName = "LastTrainingStaminaTrainingPart")
    private int lastTrainingStaminaTrainingPart;

    @JacksonXmlProperty(localName = "Trainer")
    private Trainer trainer;

    @JacksonXmlProperty(localName = "SpecialTraining")
    private String specialTraining;

    @JacksonXmlProperty(localName = "Morale")
    private int morale;

    @JacksonXmlProperty(localName = "SelfConfidence")
    private int selfConfidence;

    @JacksonXmlProperty(localName = "Experience442")
    private int experience442;

    @JacksonXmlProperty(localName = "Experience433")
    private int experience433;

    @JacksonXmlProperty(localName = "Experience451")
    private int experience451;

    @JacksonXmlProperty(localName = "Experience352")
    private int experience352;

    @JacksonXmlProperty(localName = "Experience532")
    private int experience532;

    @JacksonXmlProperty(localName = "Experience343")
    private int experience343;

    @JacksonXmlProperty(localName = "Experience541")
    private int experience541;

    @JacksonXmlProperty(localName = "Experience523")
    private int experience523;

    @JacksonXmlProperty(localName = "Experience550")
    private int experience550;

    @JacksonXmlProperty(localName = "Experience253")
    private int experience253;

}

