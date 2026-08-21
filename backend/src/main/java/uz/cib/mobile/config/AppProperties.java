package uz.cib.mobile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Security security = new Security();
    private final Mock mock = new Mock();

    public Security getSecurity() { return security; }
    public Mock getMock() { return mock; }

    public static class Security {
        private String otpPepper;
        private String pinPepper;
        private String devicePepper;
        private final Jwt jwt = new Jwt();

        public String getOtpPepper() { return otpPepper; }
        public void setOtpPepper(String otpPepper) { this.otpPepper = otpPepper; }
        public String getPinPepper() { return pinPepper; }
        public void setPinPepper(String pinPepper) { this.pinPepper = pinPepper; }
        public String getDevicePepper() { return devicePepper; }
        public void setDevicePepper(String devicePepper) { this.devicePepper = devicePepper; }
        public Jwt getJwt() { return jwt; }

        public static class Jwt {
            private String issuer = "cib-mobile-auth";
            private String audience = "cib-mobile-app";
            private String keyId = "cib-auth-key-1";
            private String privateKeyPkcs8Base64;
            private String publicKeyX509Base64;
            private boolean allowEphemeralKeys;

            public String getIssuer() { return issuer; }
            public void setIssuer(String issuer) { this.issuer = issuer; }
            public String getAudience() { return audience; }
            public void setAudience(String audience) { this.audience = audience; }
            public String getKeyId() { return keyId; }
            public void setKeyId(String keyId) { this.keyId = keyId; }
            public String getPrivateKeyPkcs8Base64() { return privateKeyPkcs8Base64; }
            public void setPrivateKeyPkcs8Base64(String value) { this.privateKeyPkcs8Base64 = value; }
            public String getPublicKeyX509Base64() { return publicKeyX509Base64; }
            public void setPublicKeyX509Base64(String value) { this.publicKeyX509Base64 = value; }
            public boolean isAllowEphemeralKeys() { return allowEphemeralKeys; }
            public void setAllowEphemeralKeys(boolean allowEphemeralKeys) { this.allowEphemeralKeys = allowEphemeralKeys; }
        }
    }

    public static class Mock {
        private final Sms sms = new Sms();
        private final Myid myid = new Myid();
        public Sms getSms() { return sms; }
        public Myid getMyid() { return myid; }

        public static class Sms {
            private boolean enabled;
            private String fixedOtp;
            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            public String getFixedOtp() { return fixedOtp; }
            public void setFixedOtp(String fixedOtp) { this.fixedOtp = fixedOtp; }
        }

        public static class Myid {
            private boolean enabled;
            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
        }
    }
}
