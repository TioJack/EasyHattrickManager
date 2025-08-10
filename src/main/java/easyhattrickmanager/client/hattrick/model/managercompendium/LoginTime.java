package easyhattrickmanager.client.hattrick.model.managercompendium;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

@Data
public class LoginTime {

    @JacksonXmlText
    private String value;

}

