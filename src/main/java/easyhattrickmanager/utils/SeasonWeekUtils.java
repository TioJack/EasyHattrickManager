package easyhattrickmanager.utils;

import static java.lang.Integer.parseInt;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class SeasonWeekUtils {

    public static String convertToSeasonWeek(ZonedDateTime date) {
        ZonedDateTime referenceDate = ZonedDateTime.of(2025, 5, 5, 0, 0, 0, 0, ZoneId.of("Europe/Madrid"));
        int referenceSeason = 91;
        int referenceWeekInSeason = 2;
        ZonedDateTime startOfReferenceSeason = referenceDate.minusWeeks(referenceWeekInSeason - 1);
        long weeksSinceReference = ChronoUnit.WEEKS.between(startOfReferenceSeason, date);
        ZonedDateTime calculatedStartOfWeek = startOfReferenceSeason.plusWeeks(weeksSinceReference);
        if (date.isBefore(calculatedStartOfWeek)) {
            weeksSinceReference -= 1;
        }
        long seasonsSinceReference = weeksSinceReference / 16;
        long weekInSeason = (weeksSinceReference % 16) + 1;
        if (weekInSeason <= 0) {
            weekInSeason += 16;
            seasonsSinceReference -= 1;
        }
        long season = referenceSeason + seasonsSinceReference;
        return String.format("S%03dW%02d", season, weekInSeason);
    }

    public static ZonedDateTime convertFromSeasonWeek(String seasonWeek) {
        if (!seasonWeek.matches("S\\d{3}W\\d{2}")) {
            throw new IllegalArgumentException("Incorrect format SxxxWyy");
        }
        int season = parseInt(seasonWeek.substring(1, 4));
        int week = parseInt(seasonWeek.substring(5, 7));
        if (week < 1 || week > 16) {
            throw new IllegalArgumentException("Week will be between 1-16");
        }
        ZonedDateTime referenceDate = ZonedDateTime.of(2025, 5, 5, 0, 0, 0, 0, ZoneId.of("Europe/Madrid"));
        int referenceSeason = 91;
        int referenceWeekInSeason = 2;
        ZonedDateTime startOfReferenceSeason = referenceDate.minusWeeks(referenceWeekInSeason - 1);
        int seasonsDifference = season - referenceSeason;
        ZonedDateTime startOfRequestedSeason = startOfReferenceSeason.plusWeeks(seasonsDifference * 16);
        ZonedDateTime startOfRequestedWeek = startOfRequestedSeason.plusWeeks(week - 1);
        return startOfRequestedWeek;
    }
}
