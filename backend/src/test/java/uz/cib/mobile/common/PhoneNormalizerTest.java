package uz.cib.mobile.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhoneNormalizerTest {
    private final PhoneNormalizer normalizer = new PhoneNormalizer();

    @Test
    void normalizesUzbekPhone() {
        assertEquals("+998901234567", normalizer.normalizeUzbekPhone("90 123 45 67"));
        assertEquals("+998901234567", normalizer.normalizeUzbekPhone("998901234567"));
    }

    @Test
    void rejectsInvalidPhone() {
        assertThrows(ApiException.class, () -> normalizer.normalizeUzbekPhone("+12025550123"));
    }
}
