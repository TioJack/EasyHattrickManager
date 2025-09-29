package easyhattrickmanager.client.hattrick.model.matchlineup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Player {

    @JacksonXmlProperty(localName = "PlayerID")
    private int playerId;

    @JacksonXmlProperty(localName = "RoleID")
    private int roleId;

    @JacksonXmlProperty(localName = "FirstName")
    private String firstName;

    @JacksonXmlProperty(localName = "LastName")
    private String lastName;

    @JacksonXmlProperty(localName = "NickName")
    private String nickName;

    @JacksonXmlProperty(localName = "RatingStars")
    private float ratingStars;

    @JacksonXmlProperty(localName = "RatingStarsEndOfMatch")
    private float ratingStarsEndOfMatch;

    @JacksonXmlProperty(localName = "Behaviour")
    private int behaviour;

}

