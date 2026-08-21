package uz.cib.mobile.auth.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.cib.mobile.auth.api.dto.RefreshRequest;
import uz.cib.mobile.auth.application.SessionService;
import uz.cib.mobile.auth.application.TokenPair;
import uz.cib.mobile.auth.engine.PolicyResolver;
import uz.cib.mobile.auth.security.DeviceIdHasher;

@RestController
@RequestMapping("/api/v1/auth/token")
public class TokenController {
    private final SessionService sessions;
    private final PolicyResolver policies;
    private final DeviceIdHasher deviceHasher;

    public TokenController(SessionService sessions, PolicyResolver policies, DeviceIdHasher deviceHasher) {
        this.sessions = sessions;
        this.policies = policies;
        this.deviceHasher = deviceHasher;
    }

    @PostMapping("/refresh")
    public TokenPair refresh(
            @RequestHeader("X-Device-Id") String deviceId,
            @Valid @RequestBody RefreshRequest body) {
        return sessions.refresh(body.refreshToken(), deviceHasher.hash(deviceId), policies.active());
    }

    @PostMapping("/logout")
    public void logout(
            @RequestHeader("X-Device-Id") String deviceId,
            @Valid @RequestBody RefreshRequest body) {
        sessions.logout(body.refreshToken(), deviceHasher.hash(deviceId));
    }
}
