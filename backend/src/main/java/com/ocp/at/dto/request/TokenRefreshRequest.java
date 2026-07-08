package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {

    @NotBlank(message = "Le refresh token est obligatoire")
    private String refreshToken;
}
