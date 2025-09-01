package easyhattrickmanager.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class SeasonWeekUtilsTest {

    @Test
    public void convertToSeasonWeek() {
        var sw = SeasonWeekUtils.convertToSeasonWeek(LocalDateTime.parse("2021-10-08 02:45:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("Europe/Madrid")));
        var d = SeasonWeekUtils.convertFromSeasonWeek("S065W13");
        var f = true;
    }

}