package easyhattrickmanager.client.hattrick.model.translations;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class Texts {

    @JacksonXmlProperty(localName = "SkillNames")
    private List<Skill> skills;

    @JacksonXmlProperty(localName = "SkillLevels")
    private List<Level> levels;

    @JacksonXmlProperty(localName = "SkillSubLevels")
    private List<SubLevel> subLevels;

    @JacksonXmlProperty(localName = "PlayerSpecialties")
    private PlayerSpecialties playerSpecialties;

    @JacksonXmlProperty(localName = "PlayerAgreeability")
    private PlayerAgreeability playerAgreeability;

    @JacksonXmlProperty(localName = "PlayerAgressiveness")
    private PlayerAgressiveness playerAgressiveness;

    @JacksonXmlProperty(localName = "PlayerHonesty")
    private PlayerHonesty playerHonesty;

    @JacksonXmlProperty(localName = "TacticTypes")
    private TacticTypes tacticTypes;

    @JacksonXmlProperty(localName = "MatchPositions")
    private MatchPositions matchPositions;

    @JacksonXmlProperty(localName = "RatingSectors")
    private RatingSectors ratingSectors;

    @JacksonXmlProperty(localName = "TeamAttitude")
    private TeamAttitude teamAttitude;

    @JacksonXmlProperty(localName = "TeamSpirit")
    private TeamSpirit teamSpirit;

    @JacksonXmlProperty(localName = "Confidence")
    private Confidence confidence;

    @JacksonXmlProperty(localName = "TrainingTypes")
    private TrainingTypes trainingTypes;

    @JacksonXmlProperty(localName = "Sponsors")
    private Sponsors sponsors;

    @JacksonXmlProperty(localName = "FanMood")
    private FanMood fanMood;

    @JacksonXmlProperty(localName = "FanMatchExpectations")
    private FanMatchExpectations fanMatchExpectations;

    @JacksonXmlProperty(localName = "FanSeasonExpectations")
    private FanSeasonExpectations fanSeasonExpectations;

    @JacksonXmlProperty(localName = "LeagueNames")
    private List<League> leagues;

}

