package easyhattrickmanager.client.hattrick.model.playerdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class PlayerSkills {

    @JacksonXmlProperty(localName = "StaminaSkill")
    private int staminaSkill;

}

