package easyhattrickmanager.client;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import easyhattrickmanager.client.model.avatars.Avatars;
import easyhattrickmanager.client.model.managercompendium.ManagerCompendium;
import easyhattrickmanager.client.model.players.Players;
import easyhattrickmanager.client.model.stafflist.Stafflist;
import easyhattrickmanager.client.model.teamdetails.TeamDetails;
import easyhattrickmanager.client.model.training.Training;
import easyhattrickmanager.client.model.worlddetails.WorldDetails;
import easyhattrickmanager.client.model.worldlanguages.WorldLanguages;
import easyhattrickmanager.configuration.HattrickClientConfiguration;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HattrickClient {

    private final OAuth10aService oAuth10aService;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final XmlMapper XMLMAPPER = XmlMapper.builder()
        .addModule(new JavaTimeModule().addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer(DATE_TIME_FORMAT)))
        .configure(WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();

    private String getXML(OAuth1AccessToken accessToken, String file) {
        try {
            OAuthRequest request = new OAuthRequest(Verb.GET, HattrickClientConfiguration.URL + "?file=" + file);
            oAuth10aService.signRequest(accessToken, request);
            final Response response = oAuth10aService.execute(request);
            if (response.isSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("HTTP Response Code: " + response.getCode());
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TeamDetails getTeamDetails(OAuth1AccessToken accessToken) {
        try {
            String xml = this.getXML(accessToken, "teamdetails&version=3.6");
            TeamDetails teamDetails = XMLMAPPER.readValue(xml, TeamDetails.class);
            return teamDetails;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Players getPlayers(OAuth1AccessToken accessToken, int teamId) {
        try {
            String xml = this.getXML(accessToken, "players&version=2.7&actionType=view&teamID=" + teamId);
            Players players = XMLMAPPER.readValue(xml, Players.class);
            return players;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Training getTraining(OAuth1AccessToken accessToken, int teamId) {
        try {
            String xml = this.getXML(accessToken, "training&version=2.2&actionType=view&teamId=" + teamId);
            Training training = XMLMAPPER.readValue(xml, Training.class);
            return training;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Stafflist getStafflist(OAuth1AccessToken accessToken, int teamId) {
        try {
            String xml = this.getXML(accessToken, "stafflist&version=1.2&teamId=" + teamId);
            Stafflist stafflist = XMLMAPPER.readValue(xml, Stafflist.class);
            return stafflist;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public WorldDetails getWorlddetails(OAuth1AccessToken accessToken) {
        try {
            String xml = this.getXML(accessToken, "worlddetails&version=1.9&includeRegions=false");
            WorldDetails worlddetails = XMLMAPPER.readValue(xml, WorldDetails.class);
            return worlddetails;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ManagerCompendium getManagerCompendium(OAuth1AccessToken accessToken) {
        try {
            String xml = this.getXML(accessToken, "managercompendium&version=1.5");
            ManagerCompendium managerCompendium = XMLMAPPER.readValue(xml, ManagerCompendium.class);
            return managerCompendium;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public WorldLanguages getWorldLanguages(OAuth1AccessToken accessToken) {
        try {
            String xml = this.getXML(accessToken, "worldlanguages&version=1.2");
            WorldLanguages worldLanguages = XMLMAPPER.readValue(xml, WorldLanguages.class);
            return worldLanguages;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Avatars getAvatars(OAuth1AccessToken accessToken, int teamId) {
        try {
            String xml = this.getXML(accessToken, "avatars&version=1.1&actionType=players&teamId=" + teamId);
            Avatars avatars = XMLMAPPER.readValue(xml, Avatars.class);
            return avatars;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
