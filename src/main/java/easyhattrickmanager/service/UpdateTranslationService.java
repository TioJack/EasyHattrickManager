package easyhattrickmanager.service;

import static java.lang.Integer.parseInt;
import static java.util.Map.entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.lowerCase;

import com.fasterxml.jackson.databind.ObjectMapper;
import easyhattrickmanager.client.hattrick.model.translations.Translations;
import easyhattrickmanager.client.libretranslate.LibreTranslateClient;
import easyhattrickmanager.configuration.AssetsConfiguration;
import easyhattrickmanager.repository.CountryDAO;
import easyhattrickmanager.repository.LanguageDAO;
import easyhattrickmanager.repository.TranslationDAO;
import easyhattrickmanager.repository.model.Language;
import easyhattrickmanager.repository.model.LeagueCountry;
import easyhattrickmanager.repository.model.Translation;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateTranslationService {

    private final static int EN_LANGUAGE_ID = 2;
    private final static int ES_LANGUAGE_ID = 6;
    private final static int CA_LANGUAGE_ID = 66;
    private final static int DEFAULT_LANGUAGE_ID = EN_LANGUAGE_ID;
    private final static Map<Integer, String> LANGUAGE_ID_TO_LIBRE_TRANSLATE_CODE = Map.<Integer, String>ofEntries(
        entry(1, "sv"),
        entry(2, "en"),
        entry(3, "de"),
        entry(4, "it"),
        entry(5, "fr"),
        entry(6, "es"),
        entry(7, "nb"),
        entry(8, "da"),
        entry(9, "fi"),
        entry(10, "nl"),
        entry(11, "pt"),
        entry(12, "ja"),
        entry(13, "pl"),
        entry(14, "ru"),
        entry(15, "zh-Hans"),
        entry(17, "ko"),
        entry(19, "tr"),
        entry(22, "ar"),
        entry(23, "ro"),
        entry(25, "en"), // Islands
        entry(32, "en"), // Serbia
        entry(33, "hu"),
        entry(34, "el"),
        entry(35, "cs"),
        entry(36, "et"),
        entry(37, "lv"),
        entry(38, "id"),
        entry(39, "en"), // Croatia
        entry(40, "he"),
        entry(43, "bg"),
        entry(45, "sk"),
        entry(50, "pt-BR"),
        entry(51, "es"),
        entry(53, "sl"),
        entry(55, "en"), // Vietnam
        entry(56, "lt"),
        entry(57, "uk"),
        entry(58, "en"), // Bosnian
        entry(65, "en"), // Vlaams
        entry(66, "ca"),
        entry(71, "en"), // Master Tongue
        entry(74, "gl"),
        entry(75, "fa"),
        entry(83, "en"), // Macedonia
        entry(84, "en"), // Belorussian
        entry(85, "sq"),
        entry(90, "en"), // Georgian
        entry(100, "az"), // Azerbaijani
        entry(103, "es"),
        entry(109, "en"), // Frysk
        entry(110, "eu"),
        entry(111, "en"), // Lëtzebuergesch
        entry(113, "en"), // Furlan
        entry(136, "nb"),
        entry(151, "en")
    );

    private final AssetsConfiguration assetsConfiguration;
    private final HattrickService hattrickService;
    private final CountryDAO countryDAO;
    private final TranslationDAO translationDAO;
    private final LanguageDAO languageDAO;
    private final LibreTranslateClient libreTranslateClient;

    public void updateTranslationsHT() {
        List<Integer> languageIds = languageDAO.getAllLanguages().stream().map(Language::getId).toList();
        languageIds.forEach(languageId -> {
            Translations translations = hattrickService.getTranslations(languageId);
            translations.getTexts().getSkills().forEach(
                skill -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.skill-" + lowerCase(skill.getType())).value(skill.getText()).build())
            );
            translations.getTexts().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.skill-level-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.specialty").value(translations.getTexts().getPlayerSpecialties().getLabel()).build());
            translations.getTexts().getPlayerSpecialties().getItems().forEach(
                item -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.specialty-" + item.getValue()).value(item.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.agreeability").value(translations.getTexts().getPlayerAgreeability().getLabel()).build());
            translations.getTexts().getPlayerAgreeability().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.agreeability-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.agressiveness").value(translations.getTexts().getPlayerAgressiveness().getLabel()).build());
            translations.getTexts().getPlayerAgressiveness().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.agressiveness-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.honesty").value(translations.getTexts().getPlayerHonesty().getLabel()).build());
            translations.getTexts().getPlayerHonesty().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.honesty-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.tactic-types").value(translations.getTexts().getTacticTypes().getLabel()).build());
            translations.getTexts().getTacticTypes().getItems().forEach(
                item -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.tactic-types-" + item.getValue()).value(item.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.match-position").value(translations.getTexts().getMatchPositions().getLabel()).build());
            translations.getTexts().getMatchPositions().getItems().forEach(
                item -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.match-position-" + lowerCase(item.getType())).value(item.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.rating-sectors").value(translations.getTexts().getRatingSectors().getLabel()).build());
            translations.getTexts().getRatingSectors().getItems().forEach(
                item -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.rating-sector-" + lowerCase(item.getType())).value(item.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.team-attitude").value(translations.getTexts().getTeamAttitude().getLabel()).build());
            translations.getTexts().getTeamAttitude().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.team-attitude-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.team-spirit").value(translations.getTexts().getTeamSpirit().getLabel()).build());
            translations.getTexts().getTeamSpirit().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.team-spirit-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.confidence").value(translations.getTexts().getConfidence().getLabel()).build());
            translations.getTexts().getConfidence().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.confidence-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.training-type").value(translations.getTexts().getTrainingTypes().getLabel()).build());
            translations.getTexts().getTrainingTypes().getItems().forEach(
                item -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.training-type-" + item.getValue()).value(item.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.sponsors").value(translations.getTexts().getSponsors().getLabel()).build());
            translations.getTexts().getSponsors().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.sponsors-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.fan-mood").value(translations.getTexts().getFanMood().getLabel()).build());
            translations.getTexts().getFanMood().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.fan-mood-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.fan-match-expectations").value(translations.getTexts().getFanMatchExpectations().getLabel()).build());
            translations.getTexts().getFanMatchExpectations().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.fan-match-expectations-" + level.getValue()).value(level.getText()).build())
            );
            translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.fan-season-expectations").value(translations.getTexts().getFanSeasonExpectations().getLabel()).build());
            translations.getTexts().getFanSeasonExpectations().getLevels().forEach(
                level -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.fan-season-expectations-" + level.getValue()).value(level.getText()).build())
            );
            translations.getTexts().getLeagues().forEach(
                league -> translationDAO.upsert(Translation.builder().languageId(languageId).key("ht.league-" + league.getLeagueId()).value(league.getLanguageLeagueName()).build())
            );
        });
    }

    private final Map<String, String> cache = new HashMap<>();

    public void updateTranslationsEHM() {
        translationDAO.deleteEHMTranslations();
        uploadDefaultTranslations();
        Map<Integer, Map<String, String>> translations = translationDAO.getAllTranslations().stream().collect(groupingBy(Translation::getLanguageId, toMap(Translation::getKey, Translation::getValue)));
        Map<String, String> defaultTranslations = translations.get(DEFAULT_LANGUAGE_ID);
        List<Integer> languageIds = languageDAO.getAllLanguages().stream().map(Language::getId).collect(toList());
        languageIds.removeAll(List.of(EN_LANGUAGE_ID, ES_LANGUAGE_ID, CA_LANGUAGE_ID));
        String source = getLibreTranslateLanguageCode(DEFAULT_LANGUAGE_ID);
        languageIds.forEach(languageId ->
            defaultTranslations.entrySet().stream().filter(entry -> entry.getKey().startsWith("ehm.")).forEach(entry -> {
                    if (!translations.containsKey(languageId) || !translations.get(languageId).containsKey(entry.getKey())) {
                        String target = getLibreTranslateLanguageCode(languageId);
                        if (source.equals(target)) {
                            translationDAO.upsert(Translation.builder().languageId(languageId).key(entry.getKey()).value(entry.getValue()).build());
                        } else {
                            String cacheKey = target + "_" + entry.getKey();
                            String translatedValue;
                            if (cache.containsKey(cacheKey)) {
                                translatedValue = cache.get(cacheKey);
                            } else {
                                translatedValue = libreTranslateClient.translate(entry.getValue().toLowerCase(), source, target, "text", 0).getTranslatedText();
                                cache.put(cacheKey, translatedValue);
                            }
                            translationDAO.upsert(Translation.builder().languageId(languageId).key(entry.getKey()).value(translatedValue).build());
                        }
                    }
                }
            )
        );
    }

    private void uploadDefaultTranslations() {
        addTranslation_EN_ES_CA("ehm.log-in", "login", "iniciar sesión", "iniciar sessió");
        addTranslation_EN_ES_CA("ehm.user", "user", "usuario", "usuari");
        addTranslation_EN_ES_CA("ehm.password", "password", "contraseña", "contrasenya");
        addTranslation_EN_ES_CA("ehm.log-in-warning",
            "Your own credentials are required for this wizard. Do not use Hattrick credentials.",
            "se solicitan credenciales propias para este asistente. No use las credenciales de Hattrick.",
            "Se sol·liciten credencials pròpies per a aquest assistent. No utilitzeu les credencials de Hattrick.");
        addTranslation_EN_ES_CA("ehm.get-into", "login", "ingresar", "ingressar");
        addTranslation_EN_ES_CA("ehm.error-log-in", "incorrect credentials, please try again.", "credenciales incorrectas, intenta nuevamente.", "credencials incorrectes, intenta novament.");
        addTranslation_EN_ES_CA("ehm.no-account", "Don't have an account?", "¿No tienes una cuenta?", "No tens un compte?");
        addTranslation_EN_ES_CA("ehm.register-here", "register here", "regístrate aquí", "registra't aquí");
        addTranslation_EN_ES_CA("ehm.register", "register", "registro", "registre");
        addTranslation_EN_ES_CA("ehm.user-name", "user name", "nombre de usuario", "nom d'usuari");
        addTranslation_EN_ES_CA("ehm.username-required", "username is required.", "el nombre de usuario es obligatorio.", "el nom dusuari és obligatori.");
        addTranslation_EN_ES_CA("ehm.password-required", "password is required.", "la contraseña es obligatoria.", "la contrasenya és obligatòria.");
        addTranslation_EN_ES_CA("ehm.password-repeat", "repeat password", "repetir contraseña", "repetir contrasenya");
        addTranslation_EN_ES_CA("ehm.password-repeat-required", "you must repeat the password.", "debes repetir la contraseña.", "heu de repetir la contrasenya.");
        addTranslation_EN_ES_CA("ehm.password-no-match", "the passwords do not match.", "las contraseñas no coinciden.", "les contrasenyes no coincideixen.");
        addTranslation_EN_ES_CA("ehm.do-register", "register", "registrarse", "registrar-se");
        addTranslation_EN_ES_CA("ehm.user-exists",
            "the user already exists, please choose another.",
            "el usuario ya existe, por favor elija otro.",
            "l'usuari ja existeix, si us plau, trieu-ne un altre.");
        addTranslation_EN_ES_CA("ehm.error-register", "error registering user.", "error al registrar el usuario.", "error en registrar l'usuari.");
        addTranslation_EN_ES_CA("ehm.have-account", "already have an account?", "¿Ya tienes una cuenta?", "ja tens un compte?");
        addTranslation_EN_ES_CA("ehm.log-in-here", "login here", "inicia sesión aquí", "inicia sessió aquí");
        addTranslation_EN_ES_CA("ehm.processing-response", "processing response ...", "procesando respuesta ...", "processant resposta ...");
        addTranslation_EN_ES_CA("ehm.welcome", "welcome", "¡Bienvenido", "benvingut");
        addTranslation_EN_ES_CA("ehm.now-on",
            "from now on, Easy Hattrick Manager will save your teams' history on a weekly basis",
            "a partir de ahora, Easy Hattrick Manager se va a encargar semanalmente de guardar el historial de tus equipos",
            "a partir d'ara, Easy Hattrick Manager s'encarregarà setmanalment de desar l'historial dels teus equips");
        addTranslation_EN_ES_CA("ehm.log-in-here-now", "you can now login here", "ya puedes iniciar sesión aquí", "ja pots iniciar sessió aquí");
        addTranslation_EN_ES_CA("ehm.missing-params",
            "required parameters to complete registration are missing.",
            "faltan parámetros necesarios para completar el registro.",
            "manquen paràmetres necessaris per completar el registre.");
        addTranslation_EN_ES_CA("ehm.loading-data", "loading data ...", "cargando datos ...", "carregant dades ...");
        addTranslation_EN_ES_CA("ehm.error-loading-data-1", "error loading data.", "error cargando los datos.", "error carregant les dades.");
        addTranslation_EN_ES_CA("ehm.error-loading-data-2", "please try again.", "por favor, intenta nuevamente.", "si us plau, intenta novament.");
        addTranslation_EN_ES_CA("ehm.age", "age", "edad", "edat");
        addTranslation_EN_ES_CA("ehm.years", "years", "años", "anys");
        addTranslation_EN_ES_CA("ehm.days", "days", "días", "dies");
        addTranslation_EN_ES_CA("ehm.wage", "wage", "salario", "salari");
        addTranslation_EN_ES_CA("ehm.person", "a", "una persona", "un");
        addTranslation_EN_ES_CA("ehm.has", "has", "tiene", "té");
        addTranslation_EN_ES_CA("ehm.and", "and", "y", "i");
        addTranslation_EN_ES_CA("ehm.loyalty", "loyalty", "fidelidad", "fidelitat");
        addTranslation_EN_ES_CA("ehm.season", "season", "temporada", "temporada");
        addTranslation_EN_ES_CA("ehm.week", "week", "semana", "semana");
        addTranslation_EN_ES_CA("ehm.project", "project", "proyecto", "projecte");
        addTranslation_EN_ES_CA("ehm.manage-projects", "manage projects", "gestionar proyectos", "gestió de projectes");
        addTranslation_EN_ES_CA("ehm.download-data", "download data", "descargar datos", "descarregar dades");
        addTranslation_EN_ES_CA("ehm.log-out", "logout", "cerrar sesión", "tancar sessió");
        addTranslation_EN_ES_CA("ehm.navigate-help",
            "you can navigate using the right and left arrow keys on your keyboard",
            "puede navegar usando la flecha derecha y izquierda del teclado",
            "podeu navegar amb les tecles de fletxa dreta i esquerra del teclat");
        addTranslation_EN_ES_CA("ehm.list-projects", "list of projects", "lista de proyectos", "llista de projectes");
        addTranslation_EN_ES_CA("ehm.name", "name", "nombre", "nom");
        addTranslation_EN_ES_CA("ehm.team", "team", "equipo", "equip");
        addTranslation_EN_ES_CA("ehm.start", "start", "inicio", "inici");
        addTranslation_EN_ES_CA("ehm.end", "end", "fin", "fi");
        addTranslation_EN_ES_CA("ehm.weekly-update", "weekly update", "actualización semanal", "actualització setmanal");
        addTranslation_EN_ES_CA("ehm.new-config", "new config", "nueva configuración", "nova configuració");
        addTranslation_EN_ES_CA("ehm.save-return", "save changes and return", "guardar cambios y volver", "guardar canvis i tornar");
        addTranslation_EN_ES_CA("ehm.only-return", "return without saving changes", "volver sin guardar cambios", "tornar sense desar canvis");
        addTranslation_EN_ES_CA("ehm.player-filtering", "player filtering", "filtrado de jugadores", "filtratge de jugadors");
        addTranslation_EN_ES_CA("ehm.inclusive-mode", "inclusive mode", "modo incluyente", "mode inclusiu");
        addTranslation_EN_ES_CA("ehm.exclusive-mode", "exclusive mode", "modo excluyente", "mode exclusiu");
        addTranslation_EN_ES_CA("ehm.inclusive-mode-help",
            "only selected players are displayed in the player list",
            "solo los jugadores seleccionados se muestran en el listado de jugadores",
            "només els jugadors seleccionats es mostren al llistat de jugadors");
        addTranslation_EN_ES_CA("ehm.exclusive-mode-help",
            "the selected players do not appear in the player lists",
            "los jugadores seleccionados no aparecen en el listados de jugadores",
            "els jugadors seleccionats no apareixen al llistats de jugadors");
        addTranslation_EN_ES_CA("ehm.list-players", "list of players", "lista de jugadores", "llista de jugadors");
    }

    private void addTranslation_EN_ES_CA(String key, String en, String es, String ca) {
        translationDAO.upsert(Translation.builder().languageId(EN_LANGUAGE_ID).key(key).value(en).build());
        translationDAO.upsert(Translation.builder().languageId(ES_LANGUAGE_ID).key(key).value(es).build());
        translationDAO.upsert(Translation.builder().languageId(CA_LANGUAGE_ID).key(key).value(ca).build());
    }

    private String getLibreTranslateLanguageCode(int languageId) {
        return LANGUAGE_ID_TO_LIBRE_TRANSLATE_CODE.getOrDefault(languageId, "en");
    }

    public void updateI18n() {
        Map<Integer, Integer> leagueCountry = countryDAO.getAllLeagueCountry().stream().collect(toMap(LeagueCountry::getLeagueId, LeagueCountry::getCountryId));
        translationDAO.getAllTranslations().stream()
            .collect(groupingBy(Translation::getLanguageId))
            .forEach((languageId, translations) ->
                generateJsonFromTranslations(leagueCountry, translations, assetsConfiguration.getAssetsPath() + "/i18n/" + languageId + ".json")
            );
    }

    private void generateJsonFromTranslations(Map<Integer, Integer> leagueCountry, List<Translation> translations, String outputFilePath) {
        List<Translation> countries = translations.stream()
            .filter(translation -> {
                if (translation.getKey().startsWith("ht.league-")) {
                    int leagueId = parseInt(translation.getKey().replace("ht.league-", ""));
                    return leagueCountry.containsKey(leagueId);
                }
                return false;
            })
            .map(translation -> {
                int leagueId = parseInt(translation.getKey().replace("ht.league-", ""));
                return Translation.builder()
                    .languageId(translation.getLanguageId())
                    .key("ht.country-league-" + leagueCountry.get(leagueId))
                    .value("" + leagueId)
                    .build();
            }).toList();

        translations.addAll(countries);

        Map<String, String> translationMap = translations.stream()
            .sorted(Comparator.comparing(Translation::getKey, (key1, key2) -> {
                int num1 = extractNumber(key1);
                int num2 = extractNumber(key2);
                int result = key1.replaceAll("-\\d+$", "").compareTo(key2.replaceAll("-\\d+$", ""));
                return result != 0 ? result : Integer.compare(num1, num2);
            }))
            .collect(toMap(Translation::getKey, Translation::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File(outputFilePath), translationMap);
        } catch (Exception e) {
            System.err.println("Error on generateJsonFromTranslations: " + e.getMessage());
        }
    }

    private int extractNumber(String key) {
        try {
            if (key.matches(".*-\\d+$")) {
                return parseInt(key.replaceAll(".*-(\\d+)$", "$1")); // Extrae el número después del último guion
            }
        } catch (NumberFormatException e) {
            System.err.println("Error on extractNumber with key: " + key);
        }
        return -1;
    }

}
