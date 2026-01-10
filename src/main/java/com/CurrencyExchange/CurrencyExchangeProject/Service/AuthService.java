package com.CurrencyExchange.CurrencyExchangeProject.Service;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.ChangePasswordDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.CreateUserDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.ForgotPasswordDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.LoginResponseDTO;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.OtpPurpose;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    public String sendOtp(String email, OtpPurpose purpose);

    public String create(CreateUserDTO user);

    public LoginResponseDTO login(String email, String password);

    public String changePassword(ChangePasswordDTO changePasswordDTO, Authentication auth);

    public String forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    void logout(String token);
}
