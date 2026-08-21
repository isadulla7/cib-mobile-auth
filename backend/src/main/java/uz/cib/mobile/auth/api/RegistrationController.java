package uz.cib.mobile.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.cib.mobile.auth.api.dto.*;
import uz.cib.mobile.auth.application.RegistrationOrchestrator;
import uz.cib.mobile.auth.application.TokenPair;
import uz.cib.mobile.auth.security.DeviceIdHasher;

@RestController
@RequestMapping("/api/v1/auth/registration")
public class RegistrationController {
    private final RegistrationOrchestrator registration;
    private final DeviceIdHasher deviceHasher;

    public RegistrationController(RegistrationOrchestrator registration, DeviceIdHasher deviceHasher) {
        this.registration = registration;
        this.deviceHasher = deviceHasher;
    }

    @PostMapping("/start")
    public RegistrationOrchestrator.RegistrationStepResult start(
            @Valid @RequestBody StartRegistrationRequest body,
            HttpServletRequest request) {
        return registration.start(body.phone(), body.policyVersion(), request.getRemoteAddr());
    }

    @PostMapping("/{flowId}/otp/verify")
    public RegistrationOrchestrator.RegistrationStepResult verifyOtp(
            @PathVariable String flowId,
            @Valid @RequestBody OtpRequest body,
            HttpServletRequest request) {
        return registration.verifyOtp(flowId, body.otp(), request.getRemoteAddr());
    }

    @PostMapping("/{flowId}/identity")
    public RegistrationOrchestrator.RegistrationStepResult identity(
            @PathVariable String flowId,
            @Valid @RequestBody IdentityRequest body,
            HttpServletRequest request) {
        return registration.identity(flowId, body.pinfl(), body.passport(), request.getRemoteAddr());
    }

    @PostMapping("/{flowId}/password")
    public RegistrationOrchestrator.RegistrationStepResult password(
            @PathVariable String flowId,
            @Valid @RequestBody PasswordRequest body,
            HttpServletRequest request) {
        return registration.password(flowId, body.password(), request.getRemoteAddr());
    }

    @PostMapping("/{flowId}/pin")
    public TokenPair pin(
            @PathVariable String flowId,
            @RequestHeader("X-Device-Id") String deviceId,
            @Valid @RequestBody PinRequest body,
            HttpServletRequest request) {
        return registration.pinAndComplete(
                flowId, body.pin(), deviceHasher.hash(deviceId), request.getRemoteAddr());
    }
}
