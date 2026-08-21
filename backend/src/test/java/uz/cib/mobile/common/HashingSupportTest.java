package uz.cib.mobile.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashingSupportTest {
    @Test
    void hmacIsStableAndSecretDependent() {
        String a = HashingSupport.hmacSha256Hex("12345678901234567890123456789012", "value");
        String b = HashingSupport.hmacSha256Hex("12345678901234567890123456789012", "value");
        String c = HashingSupport.hmacSha256Hex("abcdefghijklmnopqrstuvwxyz123456", "value");
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(64, a.length());
    }
}
