package com.CurrencyExchange.CurrencyExchangeProject.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDTO {

    private String name;
    private String email;
    private String otp;
    private String phone;
    private String password;

}

