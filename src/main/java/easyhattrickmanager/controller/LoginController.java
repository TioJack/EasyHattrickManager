package easyhattrickmanager.controller;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import easyhattrickmanager.controller.model.SaveResponse;
import easyhattrickmanager.controller.model.TokenRequest;
import easyhattrickmanager.controller.model.UserRequest;
import easyhattrickmanager.service.JwtService;
import easyhattrickmanager.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("login")
public class LoginController {

    private final LoginService loginService;
    private final JwtService jwtService;

    @PostMapping("authenticate")
    public ResponseEntity<String> authenticate(@RequestBody UserRequest request, HttpServletRequest httpServletRequest) {
        if (loginService.isValid(request.getUsername(), request.getPassword())) {
            addHistory(request.getUsername(), httpServletRequest);
            String token = jwtService.generateToken(request.getUsername());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(SC_UNAUTHORIZED).build();
        }
    }

    private void addHistory(String username, HttpServletRequest request) {
        try {
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            String userAgent = request.getHeader("User-Agent");
            loginService.addHistory(username, ipAddress, userAgent);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @PostMapping("check")
    public ResponseEntity<String> check(@RequestBody UserRequest request) {
        if (loginService.existUserEhm(request.getUsername())) {
            return ResponseEntity.status(409).build();
        }
        String url = loginService.getAuthorizationUrl(request);
        return ResponseEntity.ok(url);
    }

    @PostMapping("save")
    public ResponseEntity<SaveResponse> save(@RequestBody TokenRequest request) {
        SaveResponse response = loginService.save(request);
        return ResponseEntity.ok(response);
    }

}
