package com.CurrencyExchange.CurrencyExchangeProject.DTO;


import com.CurrencyExchange.CurrencyExchangeProject.Enums.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpRequestDTO {
    @Email
    @NotBlank
    private String email;

    @NotNull
    private OtpPurpose purpose;
}
