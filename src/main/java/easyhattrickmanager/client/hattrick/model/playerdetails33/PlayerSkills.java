package easyhattrickmanager.client.hattrick.model.playerdetails33;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class PlayerSkills {

    @JacksonXmlProperty(localName = "StaminaSkill")
    private int staminaSkill;

    @JacksonXmlProperty(localName = "KeeperSkill")
    private int keeperSkill;

    @JacksonXmlProperty(localName = "PlaymakerSkill")
    private int playmakerSkill;

    @JacksonXmlProperty(localName = "ScorerSkill")
    private int scorerSkill;

    @JacksonXmlProperty(localName = "PassingSkill")
    private int passingSkill;

    @JacksonXmlProperty(localName = "WingerSkill")
    private int wingerSkill;

    @JacksonXmlProperty(localName = "DefenderSkill")
    private int defenderSkill;

    @JacksonXmlProperty(localName = "SetPiecesSkill")
    private int setPiecesSkill;

}

