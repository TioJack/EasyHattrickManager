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
        //translationDAO.deleteEHMTranslations();
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
        addTranslation_EN_ES_CA("ehm.arrival", "Arrival", "Llegada", "Arribada");
        addTranslation_EN_ES_CA("ehm.wage", "wage", "salario", "salari");
        addTranslation_EN_ES_CA("ehm.person", "a", "una persona", "un");
        addTranslation_EN_ES_CA("ehm.has", "has", "tiene", "té");
        addTranslation_EN_ES_CA("ehm.and", "and", "y", "i");
        addTranslation_EN_ES_CA("ehm.loyalty", "loyalty", "fidelidad", "fidelitat");
        addTranslation_EN_ES_CA("ehm.season", "season", "temporada", "temporada");
        addTranslation_EN_ES_CA("ehm.seasons", "seasons", "temporadas", "temporades");
        addTranslation_EN_ES_CA("ehm.week", "week", "semana", "semana");
        addTranslation_EN_ES_CA("ehm.weeks", "weeks", "semanas", "setmanes");
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
            "the selected players do not appear in the player list",
            "los jugadores seleccionados no aparecen en el listado de jugadores",
            "els jugadors seleccionats no apareixen al llistat de jugadors");
        addTranslation_EN_ES_CA("ehm.list-players", "list of players", "lista de jugadores", "llista de jugadors");
        addTranslation_EN_ES_CA("ehm.player-sort", "player sorting", "ordenación de jugadores", "ordenació de jugadors");
        addTranslation_EN_ES_CA("ehm.asc-mode", "ascending mode", "modo ascendente", "mode ascendent");
        addTranslation_EN_ES_CA("ehm.desc-mode", "descending mode", "modo descendente", "mode descendent");
        addTranslation_EN_ES_CA("ehm.sort-criteria", "sorting criteria", "critério de ordenación", "criteri d'ordenació");
        addTranslation_EN_ES_CA("ehm.category", "Category", "Categoría", "Categoria");
        addTranslation_EN_ES_CA("ehm.category-abbr-1", "GK", "POR", "P");
        addTranslation_EN_ES_CA("ehm.category-name-1", "Goalkeeper", "Portero", "Porter");
        addTranslation_EN_ES_CA("ehm.category-abbr-2", "WB", "DL", "LAT");
        addTranslation_EN_ES_CA("ehm.category-name-2", "Wing back", "Defensa Lateral", "Defensa lateral");
        addTranslation_EN_ES_CA("ehm.category-abbr-3", "CD", "DC", "DC");
        addTranslation_EN_ES_CA("ehm.category-name-3", "Central Defender", "Defensa Central", "Defensa central");
        addTranslation_EN_ES_CA("ehm.category-abbr-4", "W", "EXT", "EXT");
        addTranslation_EN_ES_CA("ehm.category-name-4", "Winger", "Extremo", "Extrem");
        addTranslation_EN_ES_CA("ehm.category-abbr-5", "IM", "MC", "MC");
        addTranslation_EN_ES_CA("ehm.category-name-5", "Inner Midfielder", "Medio Centro", "Mig centre");
        addTranslation_EN_ES_CA("ehm.category-abbr-6", "FW", "DEL", "DV");
        addTranslation_EN_ES_CA("ehm.category-name-6", "Forward", "Delantero", "Davanter");
        addTranslation_EN_ES_CA("ehm.category-abbr-7", "S", "S", "SUB");
        addTranslation_EN_ES_CA("ehm.category-name-7", "Substitute", "Suplente", "Substitut");
        addTranslation_EN_ES_CA("ehm.category-abbr-8", "R", "R", "RES");
        addTranslation_EN_ES_CA("ehm.category-name-8", "Reserve", "Reserva", "Reserva");
        addTranslation_EN_ES_CA("ehm.category-abbr-9", "E1", "E1", "E1");
        addTranslation_EN_ES_CA("ehm.category-name-9", "Extra1", "Extra1", "Extra1");
        addTranslation_EN_ES_CA("ehm.category-abbr-10", "E2", "E2", "E2");
        addTranslation_EN_ES_CA("ehm.category-name-10", "Extra2", "Extra2", "Extra2");
        addTranslation_EN_ES_CA("ehm.category-abbr-11", "T1", "EN1", "Ent1");
        addTranslation_EN_ES_CA("ehm.category-name-11", "Trainee1", "Entrenable1", "Entrenant1");
        addTranslation_EN_ES_CA("ehm.category-abbr-12", "T2", "EN2", "Ent2");
        addTranslation_EN_ES_CA("ehm.category-name-12", "Trainee2", "Entrenable2", "Entrenant2");
        addTranslation_EN_ES_CA("ehm.category-abbr-13", "C", "FE", "E");
        addTranslation_EN_ES_CA("ehm.category-name-13", "Coach Prospect", "Futuro Entrenador", "Futur entrenador");
        addTranslation_EN_ES_CA("ehm.shirt-number", "Shirt number", "número de camiseta", "dorsal");
        addTranslation_EN_ES_CA("ehm.full-name", "full name", "nombre completo", "nom complet");
        addTranslation_EN_ES_CA("ehm.first-name", "first name", "nombre", "nom");
        addTranslation_EN_ES_CA("ehm.last-name", "last name", "apellidos", "cognoms");
        addTranslation_EN_ES_CA("ehm.nick-name", "nick name", "apodo", "sobrenom");
        addTranslation_EN_ES_CA("ehm.player-id", "player ID", "ID del jugador", "ID del jugador");
        addTranslation_EN_ES_CA("ehm.cards", "cards", "tarjetas", "targetes");
        addTranslation_EN_ES_CA("ehm.injuries", "injuries", "lesiones", "lesions");
        addTranslation_EN_ES_CA("ehm.tsi", "TSI", "TSI", "TSI");
        addTranslation_EN_ES_CA("ehm.htms-ability", "HTMS ability", "habilidad HTMS", "habilitat HTMS");
        addTranslation_EN_ES_CA("ehm.htms-potential", "HTMS potential", "potencial HTMS", "potencial HTMS");
        addTranslation_EN_ES_CA("ehm.update-data", "update data", "actualizar datos", "actualitzar dades");
        addTranslation_EN_ES_CA("ehm.update-ok", "update completed successfully!", "¡Actualización completada exitosamente!", "actualització completada correctament!");
        addTranslation_EN_ES_CA("ehm.update-fail", "update failed, please try again.", "la actualización falló, inténtelo nuevamente.", "l'actualització ha fallat, torna-ho a intentar.");
        addTranslation_EN_ES_CA("ehm.no-remember-password",
            "If you don't remember your password, please register again. Your EHM data is linked to your Hattrick user, so by registering again, you'll recover all the data you already had in EHM.",
            "Si no recuerdas tu contraseña, regístrate de nuevo. Tus datos de EHM están vinculados a tu usuario de Hattrick, así que al registrarte de nuevo, recuperarás todos los datos que ya tenías en EHM.",
            "Si no recordes la teva contrasenya, torna a registrar-te. Les teves dades d'EHM estan vinculades al teu usuari de Hattrick, així que si et registres de nou, recuperaràs totes les dades que ja tenies a l'EHM.");
        addTranslation_EN_ES_CA("ehm.manual-1010", "manual of the", "manual del", "manual de l'");
        addTranslation_EN_ES_CA("ehm.manual-1020",
            "this manual explains the main features of Easy Hattrick Manager; hereafter we will refer to it as",
            "en este manual se explican las funciones principales del Easy Hattrick Manager, en adelante nos referiremos a él como",
            "en aquest manual s'expliquen les funcions principals de l'Easy Hattrick Manager; d'ara endavant ens hi referirem com a");
        addTranslation_EN_ES_CA("ehm.manual-1029", "index", "índice", "índex");
        addTranslation_EN_ES_CA("ehm.manual-1030", "main screen", "pantalla principal", "pantalla principal");
        addTranslation_EN_ES_CA("ehm.manual-1040", "main menu", "menú principal", "menú principal");
        addTranslation_EN_ES_CA("ehm.manual-1050", "project management", "gestión de proyectos", "gestió de projectes");
        addTranslation_EN_ES_CA("ehm.manual-1060", "player sorting", "ordenación de jugadores", "ordenació de jugadors");
        addTranslation_EN_ES_CA("ehm.manual-1070", "player filtering", "filtrado de jugadores", "filtrat de jugadors");
        addTranslation_EN_ES_CA("ehm.manual-1080", "time point selector", "selector del momento temporal", "selector del moment temporal");
        addTranslation_EN_ES_CA("ehm.manual-1090", "project selector", "selector de proyecto", "selector de projecte");
        addTranslation_EN_ES_CA("ehm.manual-1100",
            "once registration and sign-in are completed, you arrive at the main page.",
            "una vez realizado el registro y el inicio de sesión, se llega a la página principal.",
            "un cop fet el registre i l'inici de sessió, s'arriba a la pàgina principal.");
        addTranslation_EN_ES_CA("ehm.manual-1110", "player list", "lista de jugadores", "llista de jugadors");
        addTranslation_EN_ES_CA("ehm.manual-1120",
            "a core part of EHM; it displays all information for each player as a card.",
            "parte fundamental del EHM, en ella se muestra toda la información de cada jugador en forma de tarjeta.",
            "part fonamental de l'EHM; s'hi mostra tota la informació de cada jugador en forma de targeta.");
        addTranslation_EN_ES_CA("ehm.manual-1130",
            "the information shown corresponds to the selected project and selected time point.",
            "la información mostrada corresponde al proyecto seleccionado y al momento temporal seleccionado.",
            "la informació mostrada correspon al projecte seleccionat i al moment temporal seleccionat.");
        addTranslation_EN_ES_CA("ehm.manual-1140", "access to the main menu.", "acceso al menú principal.", "accés al menú principal.");
        addTranslation_EN_ES_CA("ehm.manual-1150", "access to player sorting.", "acceso a la ordenación de jugadores.", "accés a l'ordenació de jugadors.");
        addTranslation_EN_ES_CA("ehm.manual-1160", "access to player filtering.", "acceso al filtrado de jugadores.", "accés al filtrat de jugadors.");
        addTranslation_EN_ES_CA("ehm.manual-1170", "time point selector.", "selector del momento temporal.", "selector del moment temporal.");
        addTranslation_EN_ES_CA("ehm.manual-1180", "project selector.", "selector de proyecto.", "selector de projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1190", "description", "descripción", "descripció");
        addTranslation_EN_ES_CA("ehm.manual-1200", "user", "usuario", "usuari");
        addTranslation_EN_ES_CA("ehm.manual-1210", "the user who has signed in to EHM is shown.", "se muestra el usuario que ha iniciado sesión en el EHM.", "es mostra l'usuari que ha iniciat sessió a l'EHM.");
        addTranslation_EN_ES_CA("ehm.manual-1220", "language", "idioma", "idioma");
        addTranslation_EN_ES_CA("ehm.manual-1230", "here you can change the system language.", "aquí puedes cambiar el idioma del sistema.", "aquí pots canviar l'idioma del sistema.");
        addTranslation_EN_ES_CA("ehm.manual-1240", "this choice is saved for future visits.", "esta selección queda guardada para futuros accesos.", "aquesta selecció queda desada per a futurs accessos.");
        addTranslation_EN_ES_CA("ehm.manual-1250",
            "by default, the language configured in Hattrick at registration time is used.",
            "por defecto, se utiliza el idioma configurado en Hattrick en el momento del registro.",
            "per defecte, s'utilitza l'idioma configurat a Hattrick en el moment del registre.");
        addTranslation_EN_ES_CA("ehm.manual-1260",
            "regarding translations, internally they are divided into two major groups",
            "respecto a las traducciones, internamente, se dividen en dos grandes grupos",
            "pel que fa a les traduccions, internament es divideixen en dos grans grups");
        addTranslation_EN_ES_CA("ehm.manual-1270", "Hattrick texts", "textos propios de Hattrick", "textos propis de Hattrick");
        addTranslation_EN_ES_CA("ehm.manual-1280", "translation provided directly by Hattrick", "traducción proporcionada directamente por Hattrick", "traducció proporcionada directament per Hattrick");
        addTranslation_EN_ES_CA("ehm.manual-1290",
            "this includes skill names, player skill values, specialties, ...",
            "eso incluye nombres de habilidades, valores de habilidades de jugadores, especialidades,...",
            "això inclou noms d'habilitats, valors d'habilitats dels jugadors, especialitats, ...");
        addTranslation_EN_ES_CA("ehm.manual-1300",
            "this allows us to support all languages available on the platform.",
            "eso nos permite dar soporte a la totalidad de idiomas disponibles en la plataforma.",
            "això ens permet donar suport a la totalitat d'idiomes disponibles a la plataforma.");
        addTranslation_EN_ES_CA("ehm.manual-1310", "EHM texts", "textos propios del EHM", "textos propis de l'EHM");
        addTranslation_EN_ES_CA("ehm.manual-1320", "texts translated specifically for EHM", "textos traducidos específicamente para el EHM", "textos traduïts específicament per a l'EHM");
        addTranslation_EN_ES_CA("ehm.manual-1330",
            "translations into Spanish, Catalan, and English are done manually",
            "las traducciones para español, catalán e inglés se realizan de forma manual",
            "les traduccions per a espanyol, català i anglès es fan de manera manual");
        addTranslation_EN_ES_CA("ehm.manual-1340", "for other languages, the open-source tool", "para otros idiomas, se utiliza la herramienta open-source", "per a altres idiomes, s'utilitza l'eina de codi obert");
        addTranslation_EN_ES_CA("ehm.manual-1350", "starting from English.", "a partir del inglés.", "a partir de l'anglès.");
        addTranslation_EN_ES_CA("ehm.manual-1360",
            "if a language is not available, English is used by default.",
            "en caso de no estar disponible un idioma, se usa el inglés por defecto.",
            "en cas de no estar disponible un idioma, s'utilitza l'anglès per defecte.");
        addTranslation_EN_ES_CA("ehm.manual-1370", "feel free to report any translation errors.", "ser libres de reportar cualquier error en las traducciones.", "sigueu lliures de reportar qualsevol error en les traduccions.");
        addTranslation_EN_ES_CA("ehm.manual-1380", "currency", "moneda", "moneda");
        addTranslation_EN_ES_CA("ehm.manual-1390", "allows you to change the currency shown in the system.", "permite cambiar la moneda que se muestra en el sistema.", "permet canviar la moneda que es mostra al sistema.");
        addTranslation_EN_ES_CA("ehm.manual-1400",
            "by default, the currency configured in Hattrick at registration time is used.",
            "por defecto, se utiliza la moneda configurada en Hattrick en el momento del registro.",
            "per defecte, s'utilitza la moneda configurada a Hattrick en el moment del registre.");
        addTranslation_EN_ES_CA("ehm.manual-1410", "function to fetch the most recent data from Hattrick.", "función para obtener los datos más recientes desde Hattrick.", "funció per obtenir les dades més recents des d'Hattrick.");
        addTranslation_EN_ES_CA("ehm.manual-1420", "it is an option that, as a rule,", "es una opción que por norma", "és una opció que, per norma,");
        addTranslation_EN_ES_CA("ehm.manual-1430", "does not need to be used", "no hace falta utilizar", "no cal utilitzar");
        addTranslation_EN_ES_CA("ehm.manual-1440", "since EHM aims to streamline this task; therefore,", "ya que el EHM, tiene como objetivo facilitar esta labor, por ello,", "ja que l'EHM té com a objectiu facilitar aquesta tasca; per això,");
        addTranslation_EN_ES_CA("ehm.manual-1450",
            "it schedules an automatic execution to update Hattrick data.",
            "tiene programada la ejecución automática de actualización de datos de Hattrick.",
            "té programada l'execució automàtica d'actualització de dades d'Hattrick.");
        addTranslation_EN_ES_CA("ehm.manual-1460",
            "the automatic run is scheduled weekly, specifically between 1 and 3 hours after the time defined in Hattrick.",
            "la ejecución automática esta prevista semanalmente, en concreto entre 1 y 3 horas después de la hora definida en Hattrick.",
            "l'execució automàtica està prevista setmanalment, en concret entre 1 i 3 hores després de l'hora definida a Hattrick.");
        addTranslation_EN_ES_CA("ehm.manual-1470",
            "it also has a retry policy in case of process failures.",
            "también dispone de una política de reintentos, en caso de fallos en el proceso.",
            "també disposa d'una política de reintents en cas de fallades en el procés.");
        addTranslation_EN_ES_CA("ehm.manual-1480",
            "likewise, and the main reason for providing this option in the menu is to be able to trigger it manually",
            "igualmente, y el motivo principal de disponer de esta opción en el menu, es poder lanzarlo manualmente ",
            "igualment, i el motiu principal de disposar d'aquesta opció al menú és poder-lo llançar manualment");
        addTranslation_EN_ES_CA("ehm.manual-1490", "if you notice that the data is not up to date.", "en caso de que se detecte que los datos no están actualizados.", "en cas que es detecti que les dades no estan actualitzades.");
        addTranslation_EN_ES_CA("ehm.manual-1500",
            "after a manual update, you need to refresh EHM so the new data is shown.",
            "después de una actualización manual, hay que refrescar el EHM para que los nuevos datos sean mostrados.",
            "després d'una actualització manual, cal refrescar l'EHM perquè es mostrin les noves dades.");
        addTranslation_EN_ES_CA("ehm.manual-1510",
            "EHM only allows one weekly data save per team to avoid duplicate data,",
            "el EHM solo permite un guardado de datos semanal por equipo, para no tener datos repetidos,",
            "l'EHM només permet un desat de dades setmanal per equip per no tenir dades repetides,");
        addTranslation_EN_ES_CA("ehm.manual-1520",
            "and within the week, the reference is the day of Hattrick's training update.",
            "y dentro de la semana se toma como referencia el día de la actualización de entrenamiento de Hattrick.",
            "i dins de la setmana es pren com a referència el dia de l'actualització d'entrenament d'Hattrick.");
        addTranslation_EN_ES_CA("ehm.manual-1530", "allows you to download a JSON file with all the information collected from your teams.", "permite descargar un archivo JSON con toda la información recopilada de tus equipos.",
            "permet descarregar un fitxer JSON amb tota la informació recopilada dels teus equips.");
        addTranslation_EN_ES_CA("ehm.manual-1540", "it is advisable to save this file regularly as a backup.", "es recomendable guardar regularmente este archivo como copia de seguridad.", "és recomanable desar regularment aquest fitxer com a còpia de seguretat.");
        addTranslation_EN_ES_CA("ehm.manual-1550", "an example is shown below", "a continuación, se muestra un ejemplo", "a continuació, es mostra un exemple");
        addTranslation_EN_ES_CA("ehm.manual-1560", "ends the current session and returns to the sign-in page.", "finaliza la sesión actual y regresa a la página de inicio de sesión.", "finalitza la sessió actual i retorna a la pàgina d'inici de sessió.");
        addTranslation_EN_ES_CA("ehm.manual-1570", "access to this manual.", "acceso a este manual.", "accés a aquest manual.");
        addTranslation_EN_ES_CA("ehm.manual-1580", "source code", "código fuente", "codi font");
        addTranslation_EN_ES_CA("ehm.manual-1590", "link to the EHM code repository; the deployed version (vXX) is indicated.", "enlace al repositorio de código del EHM, se indica la versión desplegada (vXX).", "enllaç al repositori de codi de l'EHM; s'hi indica la versió desplegada (vXX).");
        addTranslation_EN_ES_CA("ehm.manual-1600", "creator user", "usuario creador", "usuari creador");
        addTranslation_EN_ES_CA("ehm.manual-1610", "link to the Hattrick user profile of the EHM creator.", "enlace al perfil de usuario en Hattrick del creador del EHM.", "enllaç al perfil d'usuari a Hattrick del creador de l'EHM.");
        addTranslation_EN_ES_CA("ehm.manual-1620", "this screen allows us to modify the list of projects we have in EHM.", "esta pantalla nos permite modificar la lista de proyectos que tenemos en el EHM.", "aquesta pantalla ens permet modificar la llista de projectes que tenim a l'EHM.");
        addTranslation_EN_ES_CA("ehm.manual-1630", "a project is defined as a period of time for a specific team.", "un proyecto se define como un periodo de tiempo de un equipo concreto.", "un projecte es defineix com un període de temps d'un equip concret.");
        addTranslation_EN_ES_CA("ehm.manual-1640", "it's a way to configure how data is displayed.", "se trata de una forma de configurar la visualización de los datos.", "és una manera de configurar la visualització de les dades.");
        addTranslation_EN_ES_CA("ehm.manual-1650",
            "there is no problem changing this configuration and playing with it, since the data is not altered,",
            "no hay ningún problema en cambiar esta configuración, y jugar con ella, ya que los datos no se ven alterados,",
            "no hi ha cap problema a canviar aquesta configuració i jugar-hi, ja que les dades no es veuen alterades,");
        addTranslation_EN_ES_CA("ehm.manual-1660", "I repeat: it's just a way to configure the view we want.", "repito, es solo una forma de configuración de la vista que queremos tener.", "ho repeteixo: és només una manera de configurar la vista que volem tenir.");
        addTranslation_EN_ES_CA("ehm.manual-1670",
            "you can create as many projects as you need, and it's fine if they overlap in time.",
            "se pueden crear tantos proyectos como necesitéis, y no hay problemas en que se solapen temporalmente entre ellos.",
            "es poden crear tants projectes com necessiteu, i no hi ha problemes que se solapin temporalment entre ells.");
        addTranslation_EN_ES_CA("ehm.manual-1680", "current list of configured projects", "listado actual de proyectos configurados", "llistat actual de projectes configurats");
        addTranslation_EN_ES_CA("ehm.manual-1681",
            "upon registering, a project is created for each team, starting that same week and with no end date.",
            "al registrarse, se crea un proyecto por cada equipo, con inicio esa misma semana y sin fecha de fin.",
            "en registrar-se, es crea un projecte per a cada equip, amb inici aquella mateixa setmana i sense data de fi.");
        addTranslation_EN_ES_CA("ehm.manual-1690", "area for the new configuration", "área para la nueva configuración", "àrea per a la nova configuració");
        addTranslation_EN_ES_CA("ehm.manual-1700",
            "here you can modify the project configuration. By default it loads the current configuration.",
            "aquí se puede modificar la configuración de los proyectos. Por defecto carga la configuración actual.",
            "aquí es pot modificar la configuració dels projectes. Per defecte carrega la configuració actual.");
        addTranslation_EN_ES_CA("ehm.manual-1710", "it is shown as a table, where each row corresponds to a project.", "se muestra en forma de tabla, donde cada fila corresponde a un proyecto.", "es mostra en forma de taula, on cada fila correspon a un projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1720", "the order of the projects in the list is respected when displaying them in the", "se respeta el orden de los proyectos en la lista a la hora de mostrarlos en el", "es respecta l'ordre dels projectes a la llista a l'hora de mostrar-los al");
        addTranslation_EN_ES_CA("ehm.manual-1730", "add", "añadir", "afegir");
        addTranslation_EN_ES_CA("ehm.manual-1740", "button to add a new project.", "botón para añadir un nuevo proyecto.", "botó per afegir un nou projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1750", "name", "nombre", "nom");
        addTranslation_EN_ES_CA("ehm.manual-1760", "editable project name.", "nombre editable del proyecto.", "nom editable del projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1770", "team", "equipo", "equip");
        addTranslation_EN_ES_CA("ehm.manual-1780", "dropdown to select the team the project belongs to.", "desplegable para seleccionar el equipo al que pertenece el proyecto.", "desplegable per seleccionar l'equip al qual pertany el projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1790", "start", "inicio", "inici");
        addTranslation_EN_ES_CA("ehm.manual-1800", "dropdown to select the project's start week.", "desplegable para seleccionar la semana de inicio del proyecto.", "desplegable per seleccionar la setmana d'inici del projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1810", "enable end", "habilitar fin", "habilita fi");
        addTranslation_EN_ES_CA("ehm.manual-1820", "option to enable or not the project's end week.", "opción para habilitar o no la semana de fin del proyecto.", "opció per habilitar o no la setmana de fi del projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1830",
            "if it is a current project, this option should appear disabled; that way EHM will show new data after each weekly update.",
            "si es un proyecto actual, esta opción debe aparecer deshabilitada, de ese modo, el EHM va a mostrar los nuevos datos después de cada actualización semanal.",
            "si és un projecte actual, aquesta opció ha d'aparèixer deshabilitada; d'aquesta manera, l'EHM mostrarà les noves dades després de cada actualització setmanal.");
        addTranslation_EN_ES_CA("ehm.manual-1840",
            "on the contrary, if it is a past, already finished project, this option should appear enabled so that we can select a specific end week.",
            "por lo contrario, si es un proyecto del pasado, ya terminado, esta opción debe aparecer habilitada para que podamos seleccionar una semana concreta de fin de proyecto.",
            "al contrari, si és un projecte del passat, ja acabat, aquesta opció ha d'aparèixer habilitada per poder seleccionar una setmana concreta de fi de projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1850", "end", "fin", "fi");
        addTranslation_EN_ES_CA("ehm.manual-1860", "dropdown to select the project's end week.", "desplegable para seleccionar la semana de fin de proyecto.", "desplegable per seleccionar la setmana de fi de projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1870", "delete", "eliminar", "eliminar");
        addTranslation_EN_ES_CA("ehm.manual-1880",
            "button to delete the project. Remember this action does not delete any data from the system.",
            "botón para eliminar el proyecto. Recuerde que esta acción no borra ningún dato del sistema.",
            "botó per eliminar el projecte. Recordeu que aquesta acció no esborra cap dada del sistema.");
        addTranslation_EN_ES_CA("ehm.manual-1890", "back", "volver", "tornar");
        addTranslation_EN_ES_CA("ehm.manual-1900",
            "button to return to the EHM main page without saving the new configuration.",
            "botón para volver a la página principal del EHM sin guardar la nueva configuración.",
            "botó per tornar a la pàgina principal de l'EHM sense desar la nova configuració.");
        addTranslation_EN_ES_CA("ehm.manual-1910", "save and return", "guardar y volver", "desar i tornar");
        addTranslation_EN_ES_CA("ehm.manual-1920",
            "button to save the new configuration and return to the EHM main page.",
            "botón para guardar la nueva configuración y volver a la página principal del EHM.",
            "botó per desar la nova configuració i tornar a la pàgina principal de l'EHM.");
        addTranslation_EN_ES_CA("ehm.manual-1930",
            "player sorting lets you set the order in which players are shown in the (1) player list.",
            "la ordenación de jugadores permite establecer el orden en que se muestran los jugadores en el (1) listado de jugadores.",
            "l'ordenació de jugadors permet establir l'ordre en què es mostren els jugadors al (1) llistat de jugadors.");
        addTranslation_EN_ES_CA("ehm.manual-1940",
            "EHM takes care of saving the sorting configuration per project.",
            "el EHM se encarga de guardar la configuración de la ordenación por proyecto.",
            "l'EHM s'encarrega de desar la configuració de l'ordenació per projecte.");
        addTranslation_EN_ES_CA("ehm.manual-1950", "sorting modes", "modos de ordenación", "modes d'ordenació");
        addTranslation_EN_ES_CA("ehm.manual-1960", "there are two sorting modes, ascending or descending.", "hay dos modos de ordenación, ascendente o descendiente.", "hi ha dos modes d'ordenació, ascendent o descendent.");
        addTranslation_EN_ES_CA("ehm.manual-1970",
            "either numerically or alphabetically, depending on the chosen criterion.",
            "ya sea numéricamente o alfabéticamente, dependiendo del criterio escogido.",
            "ja sigui numèricament o alfabèticament, depenent del criteri escollit.");
        addTranslation_EN_ES_CA("ehm.manual-1980",
            "if there are empty values, they are shown at the end; this applies, for example, to sorting by shirt number if a player has no number assigned.",
            "en caso de haber valores vacíos, estos se muestran al final, esto aplica por ejemplo a la ordenación por número de camiseta, si algún jugador no tiene número asignado.",
            "en cas d'haver-hi valors buits, aquests es mostren al final; això s'aplica, per exemple, a l'ordenació per número de samarreta si algun jugador no té número assignat.");
        addTranslation_EN_ES_CA("ehm.manual-1990", "by default, ascending mode is selected.", "por defecto viene seleccionado el modo ascendente.", "per defecte ve seleccionat el mode ascendent.");
        addTranslation_EN_ES_CA("ehm.manual-2000", "lets you choose the criterion by which players are sorted.", "permite escoger el criterio por el qual se ordenan los jugadores.", "permet triar el criteri pel qual s'ordenen els jugadors.");
        addTranslation_EN_ES_CA("ehm.manual-2010",
            "the player ID is used to break ties when the chosen criterion has the same value for multiple players.",
            "se usa el ID del jugador para desempatar la ordenación en caso de que el valor del criterio escogido sea el mismo para varios jugadores.",
            "s'utilitza l'ID del jugador per desempatar l'ordenació en cas que el valor del criteri escollit sigui el mateix per a diversos jugadors.");
        addTranslation_EN_ES_CA("ehm.manual-2020", "by default, the player ID is selected.", "por defecto viene seleccionado el ID del jugador.", "per defecte ve seleccionat l'ID del jugador.");
        addTranslation_EN_ES_CA("ehm.manual-2030",
            "player filtering lets you select which players you want to show in the (1) player list.",
            "el filtrado de jugadores permite seleccionar los jugadores que se quieren mostrar en el (1) listado de jugadores.",
            "el filtratge de jugadors permet seleccionar els jugadors que es volen mostrar al (1) llistat de jugadors.");
        addTranslation_EN_ES_CA("ehm.manual-2040",
            "EHM takes care of saving the filtering configuration per project.",
            "el EHM se encarga de guardar la configuración del filtrado por proyecto.",
            "l'EHM s'encarrega de desar la configuració del filtratge per projecte.");
        addTranslation_EN_ES_CA("ehm.manual-2050", "filtering modes", "modos de filtrado", "modes de filtratge");
        addTranslation_EN_ES_CA("ehm.manual-2060",
            "there are two filtering modes: inclusive mode and exclusive mode.",
            "hay dos modos de filtrado, el modo incluyente y el modo excluyente.",
            "hi ha dos modes de filtratge: el mode incloent i el mode excloent.");
        addTranslation_EN_ES_CA("ehm.manual-2070",
            "inclusive mode is used to show only the selected players.",
            "el modo incluyente, sirve para mostar únicamente los jugadores seleccionados.",
            "el mode incloent serveix per mostrar únicament els jugadors seleccionats.");
        addTranslation_EN_ES_CA("ehm.manual-2080",
            "this mode is very useful for projects where we want to track a small group of players and ignore the rest.",
            "este modo es muy util para proyectos donde nos interesa seguir un grupo reducido de jugadores y despreocuparnos del resto.",
            "aquest mode és molt útil per a projectes on ens interessa seguir un grup reduït de jugadors i desentendre'ns de la resta.");
        addTranslation_EN_ES_CA("ehm.manual-2090",
            "exclusive mode is used to hide the selected players.",
            "el modo excluyente, sirve para no mostrar los jugadores seleccionados.",
            "el mode excloent serveix per no mostrar els jugadors seleccionats.");
        addTranslation_EN_ES_CA("ehm.manual-2100",
            "this mode is very useful for projects where we want to show all players except a few.",
            "este modo es muy util para proyectos donde queremos mostrar todos los jugadores, excepto algunos pocos.",
            "aquest mode és molt útil per a projectes on volem mostrar tots els jugadors, excepte uns pocs.");
        addTranslation_EN_ES_CA("ehm.manual-2110",
            "by default, exclusive mode is selected.",
            "por defecto viene seleccionado el modo excluyente.",
            "per defecte ve seleccionat el mode excloent.");
        addTranslation_EN_ES_CA("ehm.manual-2120",
            "shows the full list of project players to apply the selection, ordered by player ID.",
            "muestra la lista completa de jugadores del proyecto para aplicar la selección, por orden de ID de jugador.",
            "mostra la llista completa de jugadors del projecte per aplicar la selecció, per ordre d'ID de jugador.");
        addTranslation_EN_ES_CA("ehm.manual-2130",
            "this module lets us move along the project's timeline.",
            "este módulo es el que nos permite movernos por la línea temporal del proyecto.",
            "aquest mòdul és el que ens permet moure'ns per la línia temporal del projecte.");
        addTranslation_EN_ES_CA("ehm.manual-2140", "selected time point", "momento temporal seleccionado", "moment temporal seleccionat");
        addTranslation_EN_ES_CA("ehm.manual-2150", "indicates the season and week.", "indica la temporada y semana.", "indica la temporada i la setmana.");
        addTranslation_EN_ES_CA("ehm.manual-2160",
            "by default, and whenever a project is selected, the most recent week is shown.",
            "por defecto, y cada vez que se selecciona un proyecto se muestra la semana más reciente.",
            "per defecte, i cada vegada que se selecciona un projecte, es mostra la setmana més recent.");
        addTranslation_EN_ES_CA("ehm.manual-2170", "start", "inicio", "inici");
        addTranslation_EN_ES_CA("ehm.manual-2180",
            "button to go back to the start of the project's timeline.",
            "botón para retroceder hasta el inicio de la línea temporal del proyecto.",
            "botó per retrocedir fins a l'inici de la línia temporal del projecte.");
        addTranslation_EN_ES_CA("ehm.manual-2190",
            "button to go back one season on the project's timeline from the selected point,",
            "botón para retroceder una temporada en la linea temporal del proyecto desde el momento seleccionado,",
            "botó per retrocedir una temporada a la línia temporal del projecte des del moment seleccionat,");
        addTranslation_EN_ES_CA("ehm.manual-2200",
            "or, failing that, if there is less than one season of data, to the start of the project's timeline.",
            "o en su defecto, si hay menos de una temporda de datos, hasta el inicio de la linea temporal del proyecto.",
            "o, si no és possible, si hi ha menys d'una temporada de dades, fins a l'inici de la línia temporal del projecte.");
        addTranslation_EN_ES_CA("ehm.manual-2210",
            "button to go back one week on the project's timeline from the selected point.",
            "botón para retroceder una semana en la linea temporal del proyecto desde el momento seleccionado.",
            "botó per retrocedir una setmana a la línia temporal del projecte des del moment seleccionat.");
        addTranslation_EN_ES_CA("ehm.manual-2220", "you can also use the left arrow on the keyboard.", "también se puede usar la flecha izquierda del teclado.", "també es pot utilitzar la fletxa esquerra del teclat.");
        addTranslation_EN_ES_CA("ehm.manual-2230",
            "button to move forward one week on the project's timeline from the selected point.",
            "botón para avanzar una semana en la linea temporal del proyecto desde el momento seleccionado.",
            "botó per avançar una setmana a la línia temporal del projecte des del moment seleccionat.");
        addTranslation_EN_ES_CA("ehm.manual-2240", "you can also use the right arrow on the keyboard.", "también se puede usar la flecha derecha del teclado.", "també es pot utilitzar la fletxa dreta del teclat.");
        addTranslation_EN_ES_CA("ehm.manual-2250",
            "button to move forward one season on the project's timeline from the selected point,",
            "botón para avanzar una temporda en la linea temporal del proyecto desde el momento seleccionado,",
            "botó per avançar una temporada a la línia temporal del projecte des del moment seleccionat,");
        addTranslation_EN_ES_CA("ehm.manual-2260",
            "or, failing that, if there is less than one season of data, to the end of the project's timeline.",
            "o en su defecto, si hay menos de una temporada de datos, hasta el final de la linea temporal del proyecto.",
            "o, si no és possible, si hi ha menys d'una temporada de dades, fins al final de la línia temporal del projecte.");
        addTranslation_EN_ES_CA("ehm.manual-2270", "end", "fin", "fi");
        addTranslation_EN_ES_CA("ehm.manual-2280",
            "button to move forward to the end, that is, the most recent point, of the project's timeline.",
            "botón para avanzar hasta el final, es decir, el momento más reciente, de la línea temporal del proyecto.",
            "botó per avançar fins al final, és a dir, el moment més recent, de la línia temporal del projecte.");
        addTranslation_EN_ES_CA("ehm.manual-2290", "this module lets us navigate between the different projects we have configured.", "esté módulo nos permite navegar entre los diferentes proyectos que tenemos configurados.",
            "aquest mòdul ens permet navegar entre els diferents projectes que tenim configurats.");
        addTranslation_EN_ES_CA("ehm.manual-2300", "selected project", "proyecto seleccionado", "projecte seleccionat");
        addTranslation_EN_ES_CA("ehm.manual-2310",
            "by default, the first project in the list loads and, when it does, the time point selector moves to the most recent point,",
            "por defecto se carga el primer proyecto de la lista de proyectos y al hacerlo, el selector del momento temporal se sitúa en el momento más reciente,",
            "per defecte es carrega el primer projecte de la llista de projectes i, en fer-ho, el selector del moment temporal se situa en el moment més recent,");
        addTranslation_EN_ES_CA("ehm.manual-2320",
            "at the same time, the saved configurations for filter and player sorting are loaded.",
            "a su vez se cargan las configuraciones guardadas para el filtro y ordenación de jugadores.",
            "alhora es carreguen les configuracions desades per al filtre i l'ordenació de jugadors.");
        addTranslation_EN_ES_CA("ehm.manual-2330",
            "when clicking on the combo, the list of existing projects appears,",
            "al hacer click sobre el combo, aparece la lista de proyectos existente,",
            "en fer clic sobre el combo, apareix la llista de projectes existent,");
        addTranslation_EN_ES_CA("ehm.manual-2340", "keeping their order as defined in the", "manteniendo el orden de los mismos definido en la", "mantenint l'ordre dels mateixos definit a la");
        addTranslation_EN_ES_CA("ehm.select-all", "select all", "seleccionar todos", "seleccionar tots");
        addTranslation_EN_ES_CA("ehm.deselect-all", "deselect all", "deseleccionar todos", "deseleccionar tots");
        addTranslation_EN_ES_CA("ehm.invert-selection", "invert selection", "invertir selección", "invertir selecció");
        addTranslation_EN_ES_CA("ehm.level", "level", "nivel", "nivell");
        addTranslation_EN_ES_CA("ehm.staff-type-0", "coach", "entrenador", "entrenador");
        addTranslation_EN_ES_CA("ehm.staff-type-1", "assistant coach", "entrenador asistente", "entrenador assistent");
        addTranslation_EN_ES_CA("ehm.staff-type-2", "medic", "doctor", "metge");
        addTranslation_EN_ES_CA("ehm.staff-type-3", "spokesperson", "portavoz", "portaveu");
        addTranslation_EN_ES_CA("ehm.staff-type-4", "sports psychologist", "psicólogo deportivo", "psicòleg esportiu");
        addTranslation_EN_ES_CA("ehm.staff-type-5", "form coach", "preparador físico", "assistent personal dels jugadors");
        addTranslation_EN_ES_CA("ehm.staff-type-6", "financial director", "director financiero", "director financer");
        addTranslation_EN_ES_CA("ehm.staff-type-7", "tactical assistant", "asistente táctico", "assistent tàctic");
        addTranslation_EN_ES_CA("ehm.trainer-type-0", "defensive", "defensivo", "defensiu");
        addTranslation_EN_ES_CA("ehm.trainer-type-1", "offensive", "ofensivo", "ofensiu");
        addTranslation_EN_ES_CA("ehm.trainer-type-2", "neutral", "neutro", "neutre");
        addTranslation_EN_ES_CA("ehm.intensity", "intensity", "intensidad", "intensitat");
        addTranslation_EN_ES_CA("ehm.duration", "duration", "duración", "durada");
        addTranslation_EN_ES_CA("ehm.coach", "coach", "entrenador", "entrenador");
        addTranslation_EN_ES_CA("ehm.assistant-coach", "assistant coach", "entrenador asistente", "entrenador assistent");
        addTranslation_EN_ES_CA("ehm.stamina", "stamina", "resistencia", "resistència");
        addTranslation_EN_ES_CA("ehm.add-training-stage", "add training stage", "añadir etapa de entrenamiento", "afegir etapa d'entrenament");
        addTranslation_EN_ES_CA("ehm.totals", "totals", "totales", "totals");
        addTranslation_EN_ES_CA("ehm.total-weeks", "total weeks", "total semanas", "total setmanes");
        addTranslation_EN_ES_CA("ehm.total-seasons", "total seasons", "total temporadas", "total temporades");
        addTranslation_EN_ES_CA("ehm.total-wage", "total wage", "salario total", "salari total");
        addTranslation_EN_ES_CA("ehm.averages", "averages", "medias", "mitges");
        addTranslation_EN_ES_CA("ehm.players", "players", "jugadores", "jugadors");
        addTranslation_EN_ES_CA("ehm.very-low", "very low", "muy bajo", "molt baix");
        addTranslation_EN_ES_CA("ehm.low", "low", "bajo", "baix");
        addTranslation_EN_ES_CA("ehm.high", "high", "alto", "alt");
        addTranslation_EN_ES_CA("ehm.very-high", "very high", "muy alto", "molt alt");
        addTranslation_EN_ES_CA("ehm.training", "training", "entrenamiento", "entrenament");
        addTranslation_EN_ES_CA("ehm.view-players", "players", "jugadores", "jugadors");
        addTranslation_EN_ES_CA("ehm.training-planner", "training planner", "planificador de entrenamiento", "planificador d'entrenament");
        addTranslation_EN_ES_CA("ehm.training-planner-welcome", "Welcome to the Easy Hattrick Manager training planner.", "Bienvenido al planificador de entrenamiento de Easy Hattrick Manager.", "Benvingut al planificador d'entrenament d'Easy Hattrick Manager.");
        addTranslation_EN_ES_CA("ehm.training-planner-help", "To get started, add training stages using the form on the left.", "Para empezar, añade etapas de entrenamiento mediante el formulario de la izquierda.", "Per començar, afegeix etapes d'entrenament mitjançant el formulari de l'esquerra.");
        addTranslation_EN_ES_CA("ehm.lineup-ratings", "lineup / ratings", "alineación / calificaciones", "alineació / qualificacions");
        addTranslation_EN_ES_CA("ehm.lineup-parameters", "lineup parameters", "parámetros alineación", "paràmetres alineació");
        addTranslation_EN_ES_CA("ehm.calculate-best-lineup", "calculate best lineup", "calcular mejor alineación", "calcular millor alineació");
        addTranslation_EN_ES_CA("ehm.best-lineup-criteria", "best lineup criteria", "criterio mejor alineación", "criteri millor alineació");
        addTranslation_EN_ES_CA("ehm.formation", "formation", "formación", "formació");
        addTranslation_EN_ES_CA("ehm.automatic", "automatic", "automática", "automàtica");
        addTranslation_EN_ES_CA("ehm.team-spirit-sublevel", "team spirit sublevel", "subnivel del espíritu del equipo", "subnivell de l'esperit de l'equip");
        addTranslation_EN_ES_CA("ehm.team-confidence", "team confidence", "confianza del equipo", "confiança de l'equip");
        addTranslation_EN_ES_CA("ehm.team-confidence-sublevel", "team confidence sublevel", "subnivel de la confianza del equipo", "subnivell de la confiança de l'equip");
        addTranslation_EN_ES_CA("ehm.stadium", "stadium", "estadio", "estadi");
        addTranslation_EN_ES_CA("ehm.style-of-play", "style of play", "estilo de juego", "estil de joc");
        addTranslation_EN_ES_CA("ehm.evolution", "evolution", "evolución", "evolució");
        addTranslation_EN_ES_CA("ehm.best-lineup", "best lineup", "mejor alineación", "millor alineació");
        addTranslation_EN_ES_CA("ehm.statistics-weekly", "weekly statistics", "estadísticas semanales", "estadístiques setmanals");
        addTranslation_EN_ES_CA("ehm.statistics-totals", "total statistics", "estadísticas totales", "estadístiques totals");
        addTranslation_EN_ES_CA("ehm.weekly-salary", "weekly salary", "salario semanal", "salari setmanal");
        addTranslation_EN_ES_CA("ehm.no-players", "no players", "sin jugadores", "sense jugadors");
        addTranslation_EN_ES_CA("ehm.calculating-training-plan", "calculating training plan...", "calculando plan de entrenamiento...", "calculant pla d'entrenament...");
        addTranslation_EN_ES_CA("ehm.stadium-home", "home", "local", "local");
        addTranslation_EN_ES_CA("ehm.stadium-derby", "derby", "derbi", "derbi");
        addTranslation_EN_ES_CA("ehm.stadium-away", "away", "visitante", "visitant");
        addTranslation_EN_ES_CA("ehm.style-neutral", "neutral", "neutral", "neutral");
        addTranslation_EN_ES_CA("ehm.style-defensive", "defensive", "defensivo", "defensiu");
        addTranslation_EN_ES_CA("ehm.style-offensive", "offensive", "ofensivo", "ofensiu");
        addTranslation_EN_ES_CA("ehm.best-week", "best week", "mejor semana", "millor setmana");
        addTranslation_EN_ES_CA("ehm.criteria-hatstats", "HatStats", "HatStats", "HatStats");
        addTranslation_EN_ES_CA("ehm.criteria-defense-hatstats", "Defence (HatStats)", "Defensa (HatStats)", "Defensa (HatStats)");
        addTranslation_EN_ES_CA("ehm.criteria-midfield-hatstats", "Midfield (HatStats)", "Mediocampo (HatStats)", "Mig camp (HatStats)");
        addTranslation_EN_ES_CA("ehm.criteria-attack-hatstats", "Attack (HatStats)", "Ataque (HatStats)", "Atac (HatStats)");
        addTranslation_EN_ES_CA("ehm.criteria-peaso", "Peaso", "Peaso", "Peaso");
        addTranslation_EN_ES_CA("ehm.show-training-info", "show training info", "mostrar información del entrenamiento", "mostrar informació de l'entrenament");
        addTranslation_EN_ES_CA("ehm.show-training-info-help",
            "The calculation of training received takes into account minutes played, position, training type, coach level, assistant coach level, training intensity, and stamina part.",
            "El cálculo del entrenamiento recibido tiene en cuenta los minutos jugados, la posición, el tipo de entrenamiento, el nivel del entrenador, el nivel de los entrenadores asistentes, la intensidad del entreno y la parte de resistencia.",
            "El càlcul de l'entrenament rebut té en compte els minuts jugats, la posició, el tipus d'entrenament, el nivell de l'entrenador, el nivell dels entrenadors assistents, la intensitat de l'entrenament i la part de resistència.");
        addTranslation_EN_ES_CA("ehm.show-sub-skills", "show subskills", "mostrar subniveles", "mostrar subnivells");
        addTranslation_EN_ES_CA("ehm.show-sub-skills-help",
            "Schum's formulas were used for the calculation.",
            "Para el cálculo se han usado las fórmulas de Schum.",
            "Per al càlcul s'han fet servir les fórmules de Schum.");
        addTranslation_EN_ES_CA("ehm.forum", "forum", "foro", "fòrum");
        addTranslation_EN_ES_CA("ehm.forum-help",
            "Access this CHPP's forum. Updates are posted there and are open to any feedback, questions, suggestions, improvements or errors.",
            "Acceso al foro de esta CHPP. Allí se publican las actualizaciones y esta abierto a cualquier opinión, consulta, sugerencia, mejora o error.",
            "Accés al fòrum d´aquesta CHPP. Allí es publiquen les actualitzacions i està obert a qualsevol opinió, consulta, suggeriment, millora o error.");
        addTranslation_EN_ES_CA("ehm.donations", "donations", "donaciones", "donacions");
        addTranslation_EN_ES_CA("ehm.donations-help",
            "This CHPP will always be free. However, any contribution towards its maintenance and improvement is appreciated.",
            "Esta CHPP siempre va a ser gratis. Pero se agradece cualquier contribución para su mantenimiento y crecimiento.",
            "Aquesta CHPP sempre serà gratis. Però s'agraeix qualsevol contribució per al seu manteniment i millora.");
        addTranslation_EN_ES_CA("ehm.skill-level", "skill level", "nivel de habilidad", "nivell d'habilitat");
        addTranslation_EN_ES_CA("ehm.form-calculated", "calculated form", "forma calculada", "forma calculada");
        addTranslation_EN_ES_CA("ehm.form-hidden", "hidden form", "forma oculta", "forma oculta");
        addTranslation_EN_ES_CA("ehm.form-expected", "expected form", "forma esperada", "forma esperada");
        addTranslation_EN_ES_CA("ehm.main-skills-group", "skills", "habilidades", "habilitats");
        addTranslation_EN_ES_CA("ehm.status-group", "status", "estado", "estat");
        addTranslation_EN_ES_CA("ehm.trainer-skills-group", "coach", "entrenador", "entrenador");
        addTranslation_EN_ES_CA("ehm.global-skills-group", "global", "global", "global");
        addManualV2Translations();
    }

    private void addManualV2Translations() {
        addTranslation_EN_ES_CA("ehm.manual-v2-title", "Easy Hattrick Manager Manual", "Manual de Easy Hattrick Manager", "Manual d'Easy Hattrick Manager");
        addTranslation_EN_ES_CA("ehm.manual-v2-intro",
            "Updated visual manual. Every block includes an English screenshot with numbered markers and a detailed explanation for each marker.",
            "Manual visual actualizado. Cada bloque incluye una captura en inglés con marcadores numerados y una explicación detallada de cada marcador.",
            "Manual visual actualitzat. Cada bloc inclou una captura en anglès amb marcadors numerats i una explicació detallada de cada marcador.");
        addTranslation_EN_ES_CA("ehm.manual-v2-index-title", "Index", "Índice", "Índex");

        addTranslation_EN_ES_CA("ehm.manual-v2-index-1", "1. Home - overview", "1. Home - vista general", "1. Home - vista general");
        addTranslation_EN_ES_CA("ehm.manual-v2-index-2", "2. Main menu and quick actions", "2. Menú principal y acciones rápidas", "2. Menú principal i accions ràpides");
        addTranslation_EN_ES_CA("ehm.manual-v2-index-3", "3. Player sorting", "3. Orden de jugadores", "3. Ordenació de jugadors");
        addTranslation_EN_ES_CA("ehm.manual-v2-index-4", "4. Player filtering", "4. Filtrado de jugadores", "4. Filtratge de jugadors");
        addTranslation_EN_ES_CA("ehm.manual-v2-index-5", "5. Training Planner, player prime and lineup prime", "5. Training Planner, prime de jugador y prime de alineación", "5. Training Planner, prime de jugador i prime d'alineació");
        addTranslation_EN_ES_CA("ehm.manual-v2-index-6", "6. Project management", "6. Gestión de proyectos", "6. Gestió de projectes");
        addTranslation_EN_ES_CA("ehm.manual-v2-index-7", "7. Player historical detail", "7. Detalle histórico de jugador", "7. Detall històric de jugador");

        addTranslation_EN_ES_CA("ehm.manual-v2-section-1", "1. Home - overview", "1. Home - vista general", "1. Home - vista general");
        addTranslation_EN_ES_CA("ehm.manual-v2-section-2", "2. Main menu and quick actions", "2. Menú principal y acciones rápidas", "2. Menú principal i accions ràpides");
        addTranslation_EN_ES_CA("ehm.manual-v2-section-3", "3. Player sorting", "3. Orden de jugadores", "3. Ordenació de jugadors");
        addTranslation_EN_ES_CA("ehm.manual-v2-section-4", "4. Player filtering", "4. Filtrado de jugadores", "4. Filtratge de jugadors");
        addTranslation_EN_ES_CA("ehm.manual-v2-section-5", "5. Training Planner, player prime and lineup prime", "5. Training Planner, prime de jugador y prime de alineación", "5. Training Planner, prime de jugador i prime d'alineació");
        addTranslation_EN_ES_CA("ehm.manual-v2-subsection-5b", "5.2 Lineup and final-prime analysis", "5.2 Análisis de alineación y prime final", "5.2 Anàlisi d'alineació i prime final");
        addTranslation_EN_ES_CA("ehm.manual-v2-section-6", "6. Project management", "6. Gestión de proyectos", "6. Gestió de projectes");
        addTranslation_EN_ES_CA("ehm.manual-v2-section-7", "7. Player historical detail", "7. Detalle histórico de jugador", "7. Detall històric de jugador");

        addTranslation_EN_ES_CA("ehm.manual-v2-item-1",
            "Active project selector. Changing project reloads the full workspace context, including project timeline, calculated values, and the saved player filter/sort configuration associated with that project.",
            "Selector de proyecto activo. Al cambiar de proyecto se recarga todo el contexto de trabajo, incluyendo la línea temporal del proyecto, los valores calculados y la configuración guardada de filtro/orden de jugadores asociada a ese proyecto.",
            "Selector de projecte actiu. En canviar de projecte es recarrega tot el context de treball, incloent la línia temporal del projecte, els valors calculats i la configuració desada de filtre/ordre de jugadors associada a aquest projecte.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-2",
            "View-mode switch. It alternates between the operational players view (day-to-day monitoring) and the Training Planner simulation view (long-term planning and optimization).",
            "Conmutador de modo de vista. Alterna entre la vista operativa de jugadores (seguimiento diario) y la vista de simulación de Training Planner (planificación y optimización a largo plazo).",
            "Commutador de mode de vista. Alterna entre la vista operativa de jugadors (seguiment diari) i la vista de simulació de Training Planner (planificació i optimització a llarg termini).");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-3",
            "Season/week navigator with quick jumps. Use it to move across the selected project timeline and compare past, current and projected states. Quick controls allow jump-to-start, jump-by-season and jump-to-end movements.",
            "Navegador de temporada/semana con saltos rápidos. Úsalo para moverte por la línea temporal del proyecto seleccionado y comparar estados pasados, actuales y proyectados. Los controles rápidos permiten saltar al inicio, por temporada y al final.",
            "Navegador de temporada/setmana amb salts ràpids. Fes-lo servir per moure't per la línia temporal del projecte seleccionat i comparar estats passats, actuals i projectats. Els controls ràpids permeten saltar a l'inici, per temporada i al final.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-4",
            "Shortcut to open the player filtering panel. Filtering is applied instantly to visible player cards and is persisted per project so each project can keep its own focus group.",
            "Acceso directo para abrir el panel de filtrado de jugadores. El filtrado se aplica al instante sobre las tarjetas visibles y se guarda por proyecto para que cada proyecto mantenga su propio grupo de enfoque.",
            "Accés directe per obrir el panell de filtratge de jugadors. El filtratge s'aplica a l'instant sobre les targetes visibles i es desa per projecte perquè cada projecte mantingui el seu propi grup de focus.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-5",
            "Shortcut to open the sorting panel. Sorting criterion and direction are applied in real time to the player list and are also persisted per project.",
            "Acceso directo para abrir el panel de orden. El criterio y el sentido de ordenación se aplican en tiempo real sobre la lista de jugadores y también se guardan por proyecto.",
            "Accés directe per obrir el panell d'ordenació. El criteri i el sentit d'ordenació s'apliquen en temps real sobre la llista de jugadors i també es desen per projecte.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-6",
            "Main menu toggle. Opens the global settings and quick actions area: language, currency, date format, visibility options, data update/export and navigation links.",
            "Conmutador del menú principal. Abre la zona de ajustes globales y acciones rápidas: idioma, moneda, formato de fecha, opciones de visibilidad, actualización/exportación de datos y enlaces de navegación.",
            "Commutador del menú principal. Obre la zona d'ajustos globals i accions ràpides: idioma, moneda, format de data, opcions de visibilitat, actualització/exportació de dades i enllaços de navegació.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-7",
            "Weekly summary cards. They aggregate team totals and averages (economy, age, form, stamina, etc.) plus contextual staff/training information for the currently selected week.",
            "Tarjetas resumen semanales. Agregan totales y medias del equipo (economía, edad, forma, resistencia, etc.) junto con la información contextual de staff/entrenamiento para la semana actualmente seleccionada.",
            "Targetes resum setmanals. Agreguen totals i mitjanes de l'equip (economia, edat, forma, resistència, etc.) juntament amb la informació contextual de staff/entrenament per a la setmana actualment seleccionada.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-8",
            "Player cards grid. Each card combines personality/status data, key skill levels, HTMS indicators and direct links to historical player detail, enabling quick scouting and follow-up.",
            "Rejilla de tarjetas de jugador. Cada tarjeta combina datos de personalidad/estado, niveles de habilidades clave, indicadores HTMS y enlaces directos al detalle histórico del jugador, facilitando scouting y seguimiento rápido.",
            "Graella de targetes de jugador. Cada targeta combina dades de personalitat/estat, nivells d'habilitats clau, indicadors HTMS i enllaços directes al detall històric del jugador, facilitant scouting i seguiment ràpid.");

        addTranslation_EN_ES_CA("ehm.manual-v2-item-9",
            "Main menu panel expanded. This visible state confirms you are editing account-wide preferences and running quick actions from a single central panel.",
            "Panel del menú principal desplegado. Este estado visible confirma que estás editando preferencias globales y ejecutando acciones rápidas desde un panel central único.",
            "Panell del menú principal desplegat. Aquest estat visible confirma que estàs editant preferències globals i executant accions ràpides des d'un panell central únic.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-10",
            "Language selector. Changes UI labels and generated texts. If a translation key is missing in a specific language, English is used as fallback.",
            "Selector de idioma. Cambia etiquetas de interfaz y textos generados. Si falta una clave de traducción en un idioma concreto, se usa inglés como fallback.",
            "Selector d'idioma. Canvia etiquetes d'interfície i textos generats. Si falta una clau de traducció en un idioma concret, s'utilitza anglès com a fallback.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-11",
            "Currency selector. Converts and displays financial values in the chosen currency to simplify budget comparison between projects and periods.",
            "Selector de moneda. Convierte y muestra los valores financieros en la moneda elegida para simplificar comparativas de presupuesto entre proyectos y periodos.",
            "Selector de moneda. Converteix i mostra els valors financers a la moneda triada per simplificar comparatives de pressupost entre projectes i períodes.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-12",
            "Date format selector. Applies a consistent date style across the application, which is useful when comparing exports, screenshots or shared reports.",
            "Selector de formato de fecha. Aplica un estilo de fecha consistente en toda la aplicación, útil al comparar exportaciones, capturas o informes compartidos.",
            "Selector de format de data. Aplica un estil de data consistent a tota l'aplicació, útil en comparar exportacions, captures o informes compartits.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-13",
            "Training-info visibility toggle. Enables or hides training-related indicators on player cards, including values that depend on minutes, position and training settings.",
            "Interruptor de visibilidad de info de entrenamiento. Activa u oculta indicadores de entrenamiento en las tarjetas de jugador, incluyendo valores que dependen de minutos, posición y ajustes de entrenamiento.",
            "Interruptor de visibilitat d'info d'entrenament. Activa o amaga indicadors d'entrenament a les targetes de jugador, incloent valors que depenen de minuts, posició i ajustos d'entrenament.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-14",
            "Subskills visibility toggle. Shows or hides sublevel information for finer progression analysis and more precise interpretation of training impact.",
            "Interruptor de visibilidad de subniveles. Muestra u oculta información de subniveles para un análisis más fino de progresión y una interpretación más precisa del impacto del entrenamiento.",
            "Interruptor de visibilitat de subnivells. Mostra o amaga informació de subnivells per a una anàlisi més fina de progressió i una interpretació més precisa de l'impacte de l'entrenament.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-15",
            "Project-management entry point. Opens the project editor where timeline windows are created and maintained without modifying raw imported data.",
            "Entrada a gestión de proyectos. Abre el editor de proyectos donde se crean y mantienen ventanas temporales sin modificar los datos importados en bruto.",
            "Entrada a gestió de projectes. Obre l'editor de projectes on es creen i mantenen finestres temporals sense modificar les dades importades en brut.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-16",
            "Manual data update action. EHM already runs a scheduled weekly sync (with retry policy) a few hours after Hattrick's configured update, so this button is mainly for exceptional cases where data looks outdated. After running it, refresh the page to view newly imported values. To avoid duplicates, EHM stores at most one weekly snapshot per team, using the Hattrick training-update day as reference.",
            "Acción de actualización manual de datos. EHM ya ejecuta una sincronización semanal programada (con política de reintentos) unas horas después de la actualización configurada en Hattrick, por lo que este botón se usa sobre todo en casos excepcionales donde los datos parecen desactualizados. Tras ejecutarlo, refresca la página para ver los nuevos valores importados. Para evitar duplicados, EHM guarda como máximo un snapshot semanal por equipo, usando como referencia el día de actualización de entrenamiento de Hattrick.",
            "Acció d'actualització manual de dades. EHM ja executa una sincronització setmanal programada (amb política de reintents) unes hores després de l'actualització configurada a Hattrick, de manera que aquest botó s'utilitza principalment en casos excepcionals on les dades semblen desactualitzades. Després d'executar-lo, refresca la pàgina per veure els nous valors importats. Per evitar duplicats, EHM desa com a màxim un snapshot setmanal per equip, usant com a referència el dia d'actualització d'entrenament de Hattrick.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-17",
            "Data export action. Downloads a JSON file containing all collected team/project data currently loaded by EHM. It is recommended to save this file periodically as a backup and restore point before major project edits or experimental planning changes.",
            "Acción de exportación de datos. Descarga un archivo JSON con todos los datos de equipo/proyecto recopilados y actualmente cargados por EHM. Se recomienda guardarlo periódicamente como copia de seguridad y punto de restauración antes de hacer cambios grandes en proyectos o simulaciones.",
            "Acció d'exportació de dades. Descarrega un fitxer JSON amb totes les dades d'equip/projecte recopilades i actualment carregades per EHM. Es recomana desar-lo periòdicament com a còpia de seguretat i punt de restauració abans de fer canvis grans en projectes o simulacions.");

        addTranslation_EN_ES_CA("ehm.manual-v2-item-18",
            "Sorting-panel trigger in header. It opens ordering controls on top of the current page, so you can re-order cards without navigating away.",
            "Disparador del panel de orden en cabecera. Abre los controles de orden encima de la página actual, para reordenar tarjetas sin salir de la vista.",
            "Disparador del panell d'ordre a capçalera. Obre els controls d'ordre sobre la pàgina actual, per reordenar targetes sense sortir de la vista.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-19",
            "Order-direction selector. Choose ascending or descending mode depending on whether you need to prioritize low values or high values.",
            "Selector de dirección de orden. Elige modo ascendente o descendente según necesites priorizar valores bajos o valores altos.",
            "Selector de direcció d'ordre. Tria mode ascendent o descendent segons necessitis prioritzar valors baixos o valors alts.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-20",
            "Sorting-criteria selector. Supports identity, economy, skill, HTMS and training metrics. If several players share the same value, player ID is used as deterministic tie-breaker.",
            "Selector de criterio de orden. Soporta métricas de identidad, economía, habilidades, HTMS y entrenamiento. Si varios jugadores comparten valor, se usa el ID de jugador como desempate determinista.",
            "Selector de criteri d'ordre. Suporta mètriques d'identitat, economia, habilitats, HTMS i entrenament. Si diversos jugadors comparteixen valor, s'utilitza l'ID de jugador com a desempat determinista.");

        addTranslation_EN_ES_CA("ehm.manual-v2-item-21",
            "Filtering-panel trigger in header. Opens inclusion/exclusion controls and immediately applies the result to the visible player set.",
            "Disparador del panel de filtrado en cabecera. Abre controles de inclusión/exclusión y aplica inmediatamente el resultado sobre el conjunto visible de jugadores.",
            "Disparador del panell de filtratge a capçalera. Obre controls d'inclusió/exclusió i aplica immediatament el resultat sobre el conjunt visible de jugadors.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-22",
            "Filter-mode selector. Inclusive mode keeps only checked players; exclusive mode hides checked players and keeps the rest.",
            "Selector de modo de filtro. El modo incluyente conserva solo los jugadores marcados; el modo excluyente oculta los marcados y deja el resto.",
            "Selector de mode de filtre. El mode inclusiu conserva només els jugadors marcats; el mode excloent oculta els marcats i deixa la resta.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-23",
            "Player checklist used by the filter. The list includes all players in project scope and is intended for quick tactical subsets (for example, trainees or transfer candidates).",
            "Checklist de jugadores usada por el filtro. La lista incluye todos los jugadores en el alcance del proyecto y está pensada para subconjuntos tácticos rápidos (por ejemplo, entrenables o candidatos de mercado).",
            "Checklist de jugadors usada pel filtre. La llista inclou tots els jugadors dins l'abast del projecte i està pensada per a subconjunts tàctics ràpids (per exemple, entrenables o candidats de mercat).");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-24",
            "Bulk quick actions. Select all, clear all and invert selection greatly reduce setup time when working with large squads.",
            "Acciones masivas rápidas. Seleccionar todo, limpiar todo e invertir selección reducen mucho el tiempo de configuración en plantillas grandes.",
            "Accions massives ràpides. Seleccionar-ho tot, netejar-ho tot i invertir selecció redueixen molt el temps de configuració en plantilles grans.");

        addTranslation_EN_ES_CA("ehm.manual-v2-item-25",
            "Training Planner mode switch. Enters simulation mode where you can define staged training plans and evaluate projected skill and lineup evolution.",
            "Conmutador a modo Training Planner. Entra en modo simulación, donde puedes definir planes de entrenamiento por etapas y evaluar la evolución proyectada de habilidades y alineación.",
            "Commutador a mode Training Planner. Entra en mode simulació, on pots definir plans d'entrenament per etapes i avaluar l'evolució projectada d'habilitats i alineació.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-26",
            "Stage-builder form. In this manual example there are exactly two stages and each one spans 48 weeks (3 seasons): first playmaking, then defending.",
            "Formulario constructor de etapas. En este ejemplo del manual hay exactamente dos etapas y cada una abarca 48 semanas (3 temporadas): primero jugadas y después defensa.",
            "Formulari constructor d'etapes. En aquest exemple del manual hi ha exactament dues etapes i cadascuna abasta 48 setmanes (3 temporades): primer jugades i després defensa.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-27",
            "Stage timeline with controls. It shows configured segments and lets you adjust order, edit parameters or remove stages before recalculating projections.",
            "Línea temporal de etapas con controles. Muestra los segmentos configurados y permite ajustar orden, editar parámetros o eliminar etapas antes de recalcular proyecciones.",
            "Línia temporal d'etapes amb controls. Mostra els segments configurats i permet ajustar ordre, editar paràmetres o eliminar etapes abans de recalcular projeccions.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-28",
            "Plan summary panel. Displays end season/week and accumulated figures (for example total wage, weeks and seasons) expected at the end of the configured plan.",
            "Panel resumen del plan. Muestra temporada/semana final y magnitudes acumuladas (por ejemplo salario total, semanas y temporadas) esperadas al final del plan configurado.",
            "Panell resum del pla. Mostra temporada/setmana final i magnituds acumulades (per exemple salari total, setmanes i temporades) esperades al final del pla configurat.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-29",
            "Players-block toggle. Collapsing players is useful when you need to focus on lineup ratings, prime windows and tactical comparisons.",
            "Conmutador del bloque de jugadores. Plegar jugadores es útil cuando necesitas centrarte en ratings de alineación, ventanas prime y comparativas tácticas.",
            "Commutador del bloc de jugadors. Plegar jugadors és útil quan necessites centrar-te en ratings d'alineació, finestres prime i comparatives tàctiques.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-30",
            "Per-player stage sliders. They distribute training participation per stage and directly affect projected development for the selected player.",
            "Sliders por jugador y etapa. Distribuyen participación de entrenamiento por etapa y afectan directamente a la proyección de desarrollo del jugador seleccionado.",
            "Sliders per jugador i etapa. Distribueixen participació d'entrenament per etapa i afecten directament la projecció de desenvolupament del jugador seleccionat.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-31",
            "Initial compact player card. Represents the baseline snapshot before applying the full plan so you can compare starting vs projected state.",
            "Tarjeta compacta inicial del jugador. Representa el snapshot base antes de aplicar el plan completo para comparar estado inicial vs proyectado.",
            "Targeta compacta inicial del jugador. Representa el snapshot base abans d'aplicar el pla complet per comparar estat inicial vs projectat.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-32",
            "Projected compact player card. Represents the expected state after all configured stages are applied.",
            "Tarjeta compacta proyectada del jugador. Representa el estado esperado tras aplicar todas las etapas configuradas.",
            "Targeta compacta projectada del jugador. Representa l'estat esperat després d'aplicar totes les etapes configurades.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-33",
            "Player-level prime marker. The star highlights the personal best point for that player inside the simulated horizon, helping decide timing and role planning.",
            "Marcador de prime a nivel de jugador. La estrella resalta el mejor punto personal del jugador dentro del horizonte simulado, ayudando a decidir timing y planificación de rol.",
            "Marcador de prime a nivell de jugador. L'estrella ressalta el millor punt personal del jugador dins l'horitzó simulat, ajudant a decidir timing i planificació de rol.");

        addTranslation_EN_ES_CA("ehm.manual-v2-item-34",
            "Players panel collapsed state in lineup view. This layout maximizes vertical space for tactical charts and rating analysis.",
            "Estado plegado del panel de jugadores en vista de alineación. Este layout maximiza el espacio vertical para gráficos tácticos y análisis de ratings.",
            "Estat plegat del panell de jugadors en vista d'alineació. Aquest layout maximitza l'espai vertical per a gràfics tàctics i anàlisi de ratings.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-35",
            "Lineup/ratings section expanded. It exposes the evolution curve, best-week marker and tactical outputs for final lineup assessment.",
            "Sección de alineación/ratings desplegada. Expone curva de evolución, marcador de mejor semana y salidas tácticas para evaluar la alineación final.",
            "Secció d'alineació/ratings desplegada. Exposa corba d'evolució, marcador de millor setmana i sortides tàctiques per avaluar l'alineació final.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-36",
            "Final-lineup prime range. The highlighted band indicates the optimum time window where the projected team performance peaks.",
            "Rango prime de la alineación final. La banda resaltada indica la ventana temporal óptima donde el rendimiento proyectado del equipo alcanza su pico.",
            "Rang prime de l'alineació final. La banda ressaltada indica la finestra temporal òptima on el rendiment projectat de l'equip arriba al seu pic.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-37",
            "Best-week marker for final lineup. It points to the exact week selected as the top tactical point inside the prime range.",
            "Marcador de mejor semana para la alineación final. Señala la semana exacta seleccionada como punto táctico máximo dentro del rango prime.",
            "Marcador de millor setmana per a l'alineació final. Assenyala la setmana exacta seleccionada com a punt tàctic màxim dins el rang prime.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-38",
            "Final lineup and ratings pitches. Left field shows positional distribution of the best lineup, right field shows rating sectors (defense/midfield/attack by zone).",
            "Campos de alineación final y ratings. El campo izquierdo muestra la distribución posicional de la mejor alineación; el derecho muestra sectores de rating (defensa/medio/ataque por zona).",
            "Camps d'alineació final i ratings. El camp esquerre mostra la distribució posicional de la millor alineació; el dret mostra sectors de rating (defensa/mig/atac per zona).");

        addTranslation_EN_ES_CA("ehm.manual-v2-item-39",
            "Existing projects list. A project is a display/planning time window for a specific team, and multiple projects can overlap without altering stored historical data. At registration, EHM creates one default project per team (starting that same week, no end date).",
            "Lista de proyectos existentes. Un proyecto es una ventana temporal de visualización/planificación para un equipo concreto, y varios proyectos pueden solaparse sin alterar los datos históricos guardados. Al registrarse, EHM crea un proyecto por equipo (inicio esa misma semana y sin fecha de fin).",
            "Llista de projectes existents. Un projecte és una finestra temporal de visualització/planificació per a un equip concret, i diversos projectes es poden solapar sense alterar les dades històriques desades. En registrar-se, EHM crea un projecte per equip (inici aquella mateixa setmana i sense data de fi).");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-40",
            "Project editor table. This is the main configuration area: each row is one project, with editable name, team selector, start week and optional end week. The order defined here is the same order used later in the project selector.",
            "Tabla de edición de proyectos. Esta es la zona principal de configuración: cada fila es un proyecto, con nombre editable, selector de equipo, semana de inicio y semana de fin opcional. El orden definido aquí es el mismo orden que luego se usa en el selector de proyectos.",
            "Taula d'edició de projectes. Aquesta és la zona principal de configuració: cada fila és un projecte, amb nom editable, selector d'equip, setmana d'inici i setmana de fi opcional. L'ordre definit aquí és el mateix que després s'utilitza al selector de projectes.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-41",
            "Delete row action. Removes the selected project row from the editable configuration. This does not delete imported player/team data from the system; it only removes that project definition.",
            "Acción de borrar fila. Elimina la fila de proyecto seleccionada de la configuración editable. Esto no borra datos importados de jugadores/equipos del sistema; solo elimina esa definición de proyecto.",
            "Acció d'esborrar fila. Elimina la fila de projecte seleccionada de la configuració editable. Això no esborra dades importades de jugadors/equips del sistema; només elimina aquesta definició de projecte.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-42",
            "Return without saving. Leaves the project editor and discards every pending modification made in this session.",
            "Volver sin guardar. Sale del editor de proyectos y descarta todas las modificaciones pendientes hechas en esta sesión.",
            "Tornar sense desar. Surt de l'editor de projectes i descarta totes les modificacions pendents fetes en aquesta sessió.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-43",
            "Save and return. Persists the entire project-table configuration and then goes back to home with the updated setup available.",
            "Guardar y volver. Persiste la configuración completa de la tabla de proyectos y vuelve a home con el nuevo setup disponible.",
            "Desar i tornar. Persisteix la configuració completa de la taula de projectes i torna a home amb el nou setup disponible.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-44",
            "End-week enable checkbox. For active/current projects this is usually disabled (open-ended project so new weekly data keeps appearing). For historical/closed projects, enable it and choose an explicit end week to freeze the timeline.",
            "Checkbox de habilitación de semana final. En proyectos activos/actuales normalmente se deja desactivado (proyecto abierto para que sigan entrando semanas nuevas). En proyectos históricos/cerrados, actívalo y elige una semana final explícita para cerrar la línea temporal.",
            "Checkbox d'habilitació de setmana final. En projectes actius/actuals normalment es deixa desactivat (projecte obert perquè continuïn entrant setmanes noves). En projectes històrics/tancats, activa'l i tria una setmana final explícita per tancar la línia temporal.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-45",
            "Add new row action (+). Appends a new project line in the table so you can define another scenario (team + date window) without touching existing ones.",
            "Acción de añadir fila (+). Añade una nueva línea de proyecto en la tabla para definir otro escenario (equipo + ventana temporal) sin tocar los existentes.",
            "Acció d'afegir fila (+). Afegeix una nova línia de projecte a la taula per definir un altre escenari (equip + finestra temporal) sense tocar els existents.");

        addTranslation_EN_ES_CA("ehm.manual-v2-item-46",
            "Player header and context area. Identifies the analyzed player and anchors the historical chart to that specific profile.",
            "Cabecera y área de contexto del jugador. Identifica al jugador analizado y ancla el gráfico histórico a ese perfil concreto.",
            "Capçalera i àrea de context del jugador. Identifica el jugador analitzat i ancora el gràfic històric a aquest perfil concret.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-47",
            "Historical chart plotting area. It contains complete week-by-week time series across the selected period, enabling longitudinal progression analysis.",
            "Zona de trazado del gráfico histórico. Contiene series temporales completas semana a semana en el periodo seleccionado, permitiendo análisis longitudinal de progresión.",
            "Zona de traçat del gràfic històric. Conté sèries temporals completes setmana a setmana en el període seleccionat, permetent anàlisi longitudinal de progressió.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-48",
            "Right-side grouped legend. It controls visibility of skills, status variables, coach/global metrics and training-related indicators for cleaner comparisons.",
            "Leyenda agrupada en lateral derecho. Controla la visibilidad de habilidades, variables de estado, métricas globales/de entrenador e indicadores de entrenamiento para comparativas más limpias.",
            "Llegenda agrupada al lateral dret. Controla la visibilitat d'habilitats, variables d'estat, mètriques globals/d'entrenador i indicadors d'entrenament per a comparatives més netes.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-49",
            "Central trend-reading zone. Useful for comparing slopes, plateaus and breakpoints between multiple metrics over time.",
            "Zona central de lectura de tendencias. Útil para comparar pendientes, mesetas y puntos de ruptura entre múltiples métricas a lo largo del tiempo.",
            "Zona central de lectura de tendències. Útil per comparar pendents, altiplans i punts de ruptura entre múltiples mètriques al llarg del temps.");
        addTranslation_EN_ES_CA("ehm.manual-v2-item-50",
            "Upper high-impact area. This region usually concentrates breakthrough jumps and strategic milestones, making it ideal for fast visual diagnosis.",
            "Zona superior de alto impacto. Esta región suele concentrar saltos relevantes e hitos estratégicos, por lo que es ideal para diagnóstico visual rápido.",
            "Zona superior d'alt impacte. Aquesta regió sol concentrar salts rellevants i fites estratègiques, de manera que és ideal per a diagnòstic visual ràpid.");

        addTranslation_EN_ES_CA("ehm.manual-v2-note",
            "Texts and values can vary by language, currency, active project, selected week and source data freshness.",
            "Los textos y valores pueden variar según idioma, moneda, proyecto activo, semana seleccionada y frescura de los datos de origen.",
            "Els textos i valors poden variar segons idioma, moneda, projecte actiu, setmana seleccionada i frescor de les dades d'origen.");
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
