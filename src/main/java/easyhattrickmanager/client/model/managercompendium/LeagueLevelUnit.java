package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class LeagueLevelUnit {

    @JacksonXmlProperty(localName = "LeagueLevelUnitId")
    private int leagueLevelUnitId;

    @JacksonXmlProperty(localName = "LeagueLevelUnitName")
    private String leagueLevelUnitName;

}

