package com.CurrencyExchange.CurrencyExchangeProject.Service.Implementation;


import com.CurrencyExchange.CurrencyExchangeProject.DTO.LoginResponseDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.CreateUserDTO;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.UserRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Security.JwtUtil;
import com.CurrencyExchange.CurrencyExchangeProject.Service.AuthService;
import com.CurrencyExchange.CurrencyExchangeProject.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int OTP_TTL_MIN = 5;

    @Autowired
    private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    @Override
    public String sendOtp(String email, OtpPurpose purpose) {

        boolean exists = userRepo.findByEmail(email).isPresent();

        if (purpose == OtpPurpose.SIGNUP && exists)
            throw new RuntimeException("User already exists");

        if (purpose == OtpPurpose.FORGOT_PASSWORD && !exists)
            throw new RuntimeException("User not found");

        String key = "OTP:" + purpose + ":" + email;

        String otp = redisTemplate.opsForValue().get(key);
        if (otp == null) {
            otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            redisTemplate.opsForValue().set(key, otp, OTP_TTL_MIN, TimeUnit.MINUTES);
        }

        emailService.sendOtp(email, otp);
        return "OTP sent successfully";
    }

    @Override
    public String create(CreateUserDTO req) {

        String key = "OTP:SIGNUP:" + req.getEmail();
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) throw new RuntimeException("OTP expired");
        if (!savedOtp.equals(req.getOtp())) throw new RuntimeException("Invalid OTP");

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setMobile(req.getPhone());

        userRepo.save(user);
        redisTemplate.delete(key);

        return "User created successfully";
    }


    @Override
    public LoginResponseDTO login(String email, String password) {

        User user = (User) userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        String token = jwtUtil.generateToken(user.getId(), email);
        return new LoginResponseDTO("Login successful", token);
    }

    @Override
    public void logout(String token) {
        long ttl = jwtUtil.getRemainingTime(token);
        redisTemplate.opsForValue()
                .set("BLACKLIST:TOKEN:" + token, "LOGOUT", ttl, TimeUnit.SECONDS);
    }

    @Override
    public String changePassword(ChangePasswordDTO req, Authentication auth) {

        String email = auth.getName();

        User user = (User) userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepo.save(user);

        return "Password changed successfully";
    }


    @Override
    public String forgotPassword(ForgotPasswordDTO req) {

        User user =(User) userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String key = "OTP:FORGOT_PASSWORD:" + req.getEmail();
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) {
            throw new RuntimeException("OTP expired");
        }

        if (!savedOtp.equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepo.save(user);

        redisTemplate.delete(key);

        return "Password reset successfully";
    }

}
