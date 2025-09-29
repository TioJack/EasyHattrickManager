package easyhattrickmanager.client.hattrick.model.matchlineup;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Substitution {

    @JacksonXmlProperty(localName = "TeamID")
    private int teamId;

    @JacksonXmlProperty(localName = "SubjectPlayerID")
    private int subjectPlayerId;

    @JacksonXmlProperty(localName = "ObjectPlayerID")
    private int objectPlayerId;

    @JacksonXmlProperty(localName = "OrderType")
    private int orderType;

    @JacksonXmlProperty(localName = "NewPositionId")
    private int newPositionId;

    @JacksonXmlProperty(localName = "NewPositionBehaviour")
    private int newPositionBehaviour;

    @JacksonXmlProperty(localName = "MatchMinute")
    private int matchMinute;

    @JacksonXmlProperty(localName = "MatchPart")
    private int matchPart;

}

