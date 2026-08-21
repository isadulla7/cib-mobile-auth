package uz.cib.mobile.auth.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StartRegistrationRequest(
        @NotBlank String phone,
        @Min(1) int policyVersion
) {}
