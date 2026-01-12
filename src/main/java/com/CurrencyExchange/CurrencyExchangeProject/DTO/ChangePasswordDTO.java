package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
