package easyhattrickmanager.client.hattrick.model.matchdetail;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Booking {

    @JacksonXmlProperty(localName = "BookingPlayerID")
    private int playerId;

    @JacksonXmlProperty(localName = "BookingType")
    private int type;

    @JacksonXmlProperty(localName = "BookingMinute")
    private int minute;
}
