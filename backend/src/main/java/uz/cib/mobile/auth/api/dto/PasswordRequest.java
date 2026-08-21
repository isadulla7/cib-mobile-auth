package uz.cib.mobile.auth.api.dto;
import jakarta.validation.constraints.NotBlank;
public record PasswordRequest(@NotBlank String password) {}
