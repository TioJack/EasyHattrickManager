package easyhattrickmanager.client.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Country {

    @JacksonXmlProperty(localName = "CountryId")
    private int countryId;

    @JacksonXmlProperty(localName = "CountryName")
    private String countryName;

}

