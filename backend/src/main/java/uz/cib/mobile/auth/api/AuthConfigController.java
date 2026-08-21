package uz.cib.mobile.auth.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.cib.mobile.auth.application.AuthConfigService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthConfigController {
    private final AuthConfigService configService;

    public AuthConfigController(AuthConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/config")
    public AuthConfigService.AuthConfigResponse config() {
        return configService.current();
    }
}
