package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForgotPasswordDTO {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String otp;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
