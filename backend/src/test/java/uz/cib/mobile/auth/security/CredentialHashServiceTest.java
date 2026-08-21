package uz.cib.mobile.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import uz.cib.mobile.config.AppProperties;
import static org.junit.jupiter.api.Assertions.*;

class CredentialHashServiceTest {
    @Test
    void hashesPasswordAndPinWithoutPlaintextStorage() {
        AppProperties props = new AppProperties();
        props.getSecurity().setPinPepper("12345678901234567890123456789012");
        var encoder = new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);
        var service = new CredentialHashService(encoder, props);

        String passwordHash = service.hashPassword("Secret123!");
        String pinHash = service.hashPin("1234");

        assertNotEquals("Secret123!", passwordHash);
        assertNotEquals("1234", pinHash);
        assertTrue(service.verifyPassword("Secret123!", passwordHash));
        assertTrue(service.verifyPin("1234", pinHash));
        assertFalse(service.verifyPin("0000", pinHash));
    }
}
