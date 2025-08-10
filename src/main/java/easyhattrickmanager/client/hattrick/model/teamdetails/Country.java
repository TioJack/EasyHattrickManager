package easyhattrickmanager.client.hattrick.model.teamdetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Country {

    @JacksonXmlProperty(localName = "CountryID")
    private int countryId;

    @JacksonXmlProperty(localName = "CountryName")
    private String countryName;

}
