package easyhattrickmanager.client.hattrick.model.worlddetails;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Country {

    @JacksonXmlProperty(localName = "CountryID")
    private int countryId;

    @JacksonXmlProperty(localName = "CountryName")
    private String countryName;

    @JacksonXmlProperty(localName = "CurrencyName")
    private String currencyName;

    @JacksonXmlProperty(localName = "CurrencyRate")
    private String currencyRate;

    @JacksonXmlProperty(localName = "CountryCode")
    private String countryCode;

    @JacksonXmlProperty(localName = "DateFormat")
    private String dateFormat;

    @JacksonXmlProperty(localName = "TimeFormat")
    private String timeFormat;

}

