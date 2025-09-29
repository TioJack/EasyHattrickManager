package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class HomeTeam {

    @JacksonXmlProperty(localName = "HomeTeamID")
    private int homeTeamId;

    @JacksonXmlProperty(localName = "HomeTeamName")
    private String homeTeamName;

    @JacksonXmlProperty(localName = "DressURI")
    private String dressURI;

    @JacksonXmlProperty(localName = "Formation")
    private String formation;

    @JacksonXmlProperty(localName = "HomeGoals")
    private int homeGoals;

    @JacksonXmlProperty(localName = "TacticType")
    private int tacticType;

    @JacksonXmlProperty(localName = "TacticSkill")
    private int tacticSkill;

    @JacksonXmlProperty(localName = "RatingMidfield")
    private int ratingMidfield;

    @JacksonXmlProperty(localName = "RatingRightDef")
    private int ratingRightDef;

    @JacksonXmlProperty(localName = "RatingMidDef")
    private int ratingMidDef;

    @JacksonXmlProperty(localName = "RatingLeftDef")
    private int ratingLeftDef;

    @JacksonXmlProperty(localName = "RatingRightAtt")
    private int ratingRightAtt;

    @JacksonXmlProperty(localName = "RatingMidAtt")
    private int ratingMidAtt;

    @JacksonXmlProperty(localName = "RatingLeftAtt")
    private int ratingLeftAtt;

    @JacksonXmlProperty(localName = "RatingIndirectSetPiecesDef")
    private int ratingIndirectSetPiecesDef;

    @JacksonXmlProperty(localName = "RatingIndirectSetPiecesAtt")
    private int ratingIndirectSetPiecesAtt;

    @JacksonXmlProperty(localName = "NrOfChancesLeft")
    private int nrOfChancesLeft;

    @JacksonXmlProperty(localName = "NrOfChancesCenter")
    private int nrOfChancesCenter;

    @JacksonXmlProperty(localName = "NrOfChancesRight")
    private int nrOfChancesRight;

    @JacksonXmlProperty(localName = "NrOfChancesSpecialEvents")
    private int nrOfChancesSpecialEvents;

    @JacksonXmlProperty(localName = "NrOfChancesOther")
    private int nrOfChancesOther;

}

