package com.CurrencyExchange.CurrencyExchangeProject.Controller;


import com.CurrencyExchange.CurrencyExchangeProject.DTO.*;
import com.CurrencyExchange.CurrencyExchangeProject.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/otp")
    public String sendOtp(@RequestBody OtpRequest req) {
        return authService.sendOtp(req.getEmail(), req.getPurpose());
    }

    @PostMapping("/users")
    public String signup(@RequestBody createUserDTO dto) {
        return authService.create(dto);
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@RequestBody LoginReq req) {
        return authService.login(req.getEmail(), req.getPassword());
    }

    @PostMapping("/auth/logout")
    public String logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        authService.logout(token);
        return "Logged out successfully";
    }

    @PostMapping("/auth/change-password")
    public String changePassword(@RequestBody ChangePasswordDTO req, Authentication authentication) {
        return authService.changePassword(req, authentication);
    }

    @PostMapping("/auth/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordDTO req) {
        return authService.forgotPassword(req);
    }


}
