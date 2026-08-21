package uz.cib.mobile.auth.api.dto;
import jakarta.validation.constraints.NotBlank;
public record PinRequest(@NotBlank String pin) {}
