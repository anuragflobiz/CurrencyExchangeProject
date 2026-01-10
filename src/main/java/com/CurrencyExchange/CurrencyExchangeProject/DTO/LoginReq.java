package com.CurrencyExchange.CurrencyExchangeProject.DTO;


import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginReq {
    private String email;
    private String password;


}