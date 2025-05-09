package easyhattrickmanager.service;

import easyhattrickmanager.controller.model.DataResponse;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataService {

    private final UserDAO userDAO;

    @Value("${app.version}")
    private String appVersion;

    public DataResponse getData(String username) {
        User user = userDAO.get(username);
        return DataResponse.builder()
            .username(user.getName())
            .version(appVersion)
            .build();
    }
}
