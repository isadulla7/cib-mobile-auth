package uz.cib.mobile.auth.api.dto;
import jakarta.validation.constraints.NotBlank;
public record OtpRequest(@NotBlank String otp) {}
