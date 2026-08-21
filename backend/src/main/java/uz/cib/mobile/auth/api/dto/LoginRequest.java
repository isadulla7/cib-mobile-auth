package uz.cib.mobile.auth.api.dto;
import jakarta.validation.constraints.NotBlank;
public record LoginRequest(@NotBlank String phone, @NotBlank String password) {}
