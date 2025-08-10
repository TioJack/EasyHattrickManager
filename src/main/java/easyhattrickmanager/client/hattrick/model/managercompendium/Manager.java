package easyhattrickmanager.client.hattrick.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class Manager {

    @JacksonXmlProperty(localName = "UserId")
    private int userId;

    @JacksonXmlProperty(localName = "Loginname")
    private String loginname;

    @JacksonXmlProperty(localName = "SupporterTier")
    private String supporterTier;

    @JacksonXmlProperty(localName = "LastLogins")
    private List<LoginTime> loginTimes;

    @JacksonXmlProperty(localName = "Language")
    private Language language;

    @JacksonXmlProperty(localName = "Country")
    private Country country;

    @JacksonXmlProperty(localName = "Currency")
    private Currency currency;

    @JacksonXmlProperty(localName = "Teams")
    private List<Team> teams;

    @JacksonXmlProperty(localName = "NationalTeamCoach")
    private String nationalTeamCoach;

    @JacksonXmlProperty(localName = "NationalTeamAssistant")
    private String nationalTeamAssistant;

    @JacksonXmlProperty(localName = "Avatar")
    private Avatar avatar;

}

