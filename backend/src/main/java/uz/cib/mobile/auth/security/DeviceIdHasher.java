package uz.cib.mobile.auth.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.cib.mobile.common.ApiException;
import uz.cib.mobile.common.HashingSupport;
import uz.cib.mobile.config.AppProperties;

@Service
public class DeviceIdHasher {
    private final String pepper;
    public DeviceIdHasher(AppProperties properties) { this.pepper = properties.getSecurity().getDevicePepper(); }

    public String hash(String deviceId) {
        if (!StringUtils.hasText(deviceId) || deviceId.length() < 16 || deviceId.length() > 200) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DEVICE_ID_INVALID", "X-Device-Id is required");
        }
        return HashingSupport.hmacSha256Hex(pepper, deviceId);
    }
}
