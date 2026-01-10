package com.CurrencyExchange.CurrencyExchangeProject.Service;

import com.coinShiftProject.coinShiftProject.DTO.ChangePasswordDTO;
import com.coinShiftProject.coinShiftProject.DTO.ForgotPasswordDTO;
import com.coinShiftProject.coinShiftProject.DTO.LoginResponse;
import com.coinShiftProject.coinShiftProject.DTO.createUserDTO;
import com.coinShiftProject.coinShiftProject.enums.OtpPurpose;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    public String sendOtp(String email, OtpPurpose cause);

    public String create(createUserDTO user);

    public LoginResponse login(String email, String password);

    public String changePassword(ChangePasswordDTO req, Authentication auth);

    public String forgotPassword(ForgotPasswordDTO req);

    void logout(String token);
}
