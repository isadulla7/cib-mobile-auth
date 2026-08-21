package uz.cib.mobile.auth.api.dto;
import jakarta.validation.constraints.NotBlank;
public record IdentityRequest(@NotBlank String pinfl, @NotBlank String passport) {}
