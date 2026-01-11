package com.CurrencyExchange.CurrencyExchangeProject.Service.Implementation;


import com.CurrencyExchange.CurrencyExchangeProject.DTO.ChangePasswordDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.ForgotPasswordDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.LoginResponseDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.CreateUserDTO;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.User;
import com.CurrencyExchange.CurrencyExchangeProject.Exceptions.BadRequestException;
import com.CurrencyExchange.CurrencyExchangeProject.Exceptions.UserNotFoundException;
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
    private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    @Override
    public String sendOtp(String email, OtpPurpose purpose) {

        boolean exists = userRepository.findByEmail(email).isPresent();

        if (purpose == OtpPurpose.SIGNUP && exists)
            throw new BadRequestException("User already exists");

        if (purpose == OtpPurpose.FORGOT_PASSWORD && !exists)
            throw new UserNotFoundException("User not found");

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
    public String create(CreateUserDTO createUserDTO) {

        String key = "OTP:SIGNUP:" + createUserDTO.getEmail();
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) throw new BadRequestException("OTP expired");
        if (!savedOtp.equals(createUserDTO.getOtp())) throw new BadRequestException("Invalid OTP");

        User user = new User();
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
        user.setName(createUserDTO.getName());
        user.setMobile(createUserDTO.getPhone());

        userRepository.save(user);
        redisTemplate.delete(key);

        return "User created successfully";
    }


    @Override
    public LoginResponseDTO login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new BadRequestException("Invalid credentials");

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
    public String changePassword(ChangePasswordDTO changePasswordDTO, Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);

        return "Password changed successfully";
    }


    @Override
    public String forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {

        User user =userRepository.findByEmail(forgotPasswordDTO.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String key = "OTP:FORGOT_PASSWORD:" + forgotPasswordDTO.getEmail();
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) {
            throw new RuntimeException("OTP expired");
        }

        if (!savedOtp.equals(forgotPasswordDTO.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }

        if (passwordEncoder.matches(forgotPasswordDTO.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(forgotPasswordDTO.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(key);

        return "Password reset successfully";
    }

}
