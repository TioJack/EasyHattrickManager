package easyhattrickmanager.client;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {

    private final DateTimeFormatter formatter;

    public ZonedDateTimeDeserializer(DateTimeFormatter formatter) {
        super(ZonedDateTime.class);
        this.formatter = formatter;
    }

    @Override
    public ZonedDateTime deserialize(com.fasterxml.jackson.core.JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        String dateString = node.textValue();
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
        return ZonedDateTime.of(localDateTime, ZoneId.of("Europe/Madrid"));
    }
}
