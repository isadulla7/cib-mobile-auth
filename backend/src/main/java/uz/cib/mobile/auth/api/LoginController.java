package uz.cib.mobile.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.cib.mobile.auth.api.dto.LoginRequest;
import uz.cib.mobile.auth.api.dto.OtpRequest;
import uz.cib.mobile.auth.application.LoginOrchestrator;
import uz.cib.mobile.auth.security.DeviceIdHasher;

@RestController
@RequestMapping("/api/v1/auth/login")
public class LoginController {
    private final LoginOrchestrator login;
    private final DeviceIdHasher deviceHasher;

    public LoginController(LoginOrchestrator login, DeviceIdHasher deviceHasher) {
        this.login = login;
        this.deviceHasher = deviceHasher;
    }

    @PostMapping
    public LoginOrchestrator.LoginResult login(
            @RequestHeader("X-Device-Id") String deviceId,
            @Valid @RequestBody LoginRequest body,
            HttpServletRequest request) {
        return login.login(
                body.phone(), body.password(), deviceHasher.hash(deviceId), request.getRemoteAddr());
    }

    @PostMapping("/challenge/{challengeId}/otp")
    public LoginOrchestrator.LoginResult verifyNewDevice(
            @PathVariable String challengeId,
            @RequestHeader("X-Device-Id") String deviceId,
            @Valid @RequestBody OtpRequest body,
            HttpServletRequest request) {
        return login.verifyNewDeviceOtp(
                challengeId, body.otp(), deviceHasher.hash(deviceId), request.getRemoteAddr());
    }
}
