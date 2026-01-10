package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import lombok.Data;

@Data
public class ForgotPasswordDTO {
    private String email;
    private String newPassword;
    private String otp;
}
