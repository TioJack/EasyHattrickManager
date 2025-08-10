package easyhattrickmanager.client.hattrick.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;

@Data
public class User {

    @JacksonXmlProperty(localName = "UserID")
    private int userId;

    @JacksonXmlProperty(localName = "Language")
    private Language language;

    @JacksonXmlProperty(localName = "SupporterTier")
    private String supporterTier;

    @JacksonXmlProperty(localName = "Loginname")
    private String loginname;

    @JacksonXmlProperty(localName = "Name")
    private String name;

    @JacksonXmlProperty(localName = "ICQ")
    private String iCQ;

    @JacksonXmlProperty(localName = "SignupDate")
    private ZonedDateTime signupDate;

    @JacksonXmlProperty(localName = "ActivationDate")
    private ZonedDateTime activationDate;

    @JacksonXmlProperty(localName = "LastLoginDate")
    private ZonedDateTime lastLoginDate;

    @JacksonXmlProperty(localName = "HasManagerLicense")
    private boolean hasManagerLicense;

    @JacksonXmlProperty(localName = "NationalTeams")
    private List<NationalTeam> nationalTeams;

}
