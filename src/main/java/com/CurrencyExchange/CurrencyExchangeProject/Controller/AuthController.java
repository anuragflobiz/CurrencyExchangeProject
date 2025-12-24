package com.CurrencyExchange.CurrencyExchangeProject.Controller;


import com.CurrencyExchange.CurrencyExchangeProject.DTO.*;
import com.CurrencyExchange.CurrencyExchangeProject.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public String sendOtp(@RequestBody OtpRequestDTO req) {

        return authService.sendOtp(req.getEmail(), req.getPurpose());
    }

    @PostMapping("/signup")
    public String signup(@RequestBody CreateUserDTO dto) {
        return authService.create(dto);
    }

    @PostMapping("/auth/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO req) {
        return authService.login(req.getEmail(), req.getPassword());
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Authorization header");
        }

        authService.logout(authHeader.substring(7));
        return ResponseEntity.ok("Logged out successfully");
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
