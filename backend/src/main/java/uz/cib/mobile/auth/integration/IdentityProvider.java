package uz.cib.mobile.auth.integration;

public interface IdentityProvider {
    IdentityResult verify(String pinfl, String passport);
    record IdentityResult(boolean verified, String subjectId, String firstName, String lastName) {}
}
