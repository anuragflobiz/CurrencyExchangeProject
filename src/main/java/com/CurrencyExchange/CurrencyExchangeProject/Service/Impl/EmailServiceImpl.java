package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;

import com.coinShiftProject.coinShiftProject.Service.EmailService;
import com.coinShiftProject.coinShiftProject.enums.CurrencyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private  JavaMailSender javaMailSender;


    public void sendOtp(String toEmail,String otp){
        SimpleMailMessage msg=new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("OTP Verification");
        msg.setText("Your otp for signup in Coin Shift is "+otp+".\n OTP will be expired in next 5 minutes.");
        javaMailSender.send(msg);
    }

    @Override
    public void creditMail(String toEmail, BigDecimal amount, CurrencyCode currencyCode,
                           LocalDateTime time, BigDecimal finalAmount) {
        SimpleMailMessage msg=new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Wallet get Credited");
        msg.setText("Your wallet with Currency Code "+currencyCode+"\n got credited with amount "+
                amount+" "+currencyCode+"\n at "+time+
                "\nFinal amount in your wallet is "+finalAmount+" "+currencyCode
        );
        javaMailSender.send(msg);
    }

    @Override
    public void debitMail(String toEmail, BigDecimal amount, CurrencyCode currencyCode,
                          LocalDateTime time,BigDecimal finalAmount) {
        SimpleMailMessage msg=new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Wallet get Debited");
        msg.setText("Your wallet with Currency Code "+currencyCode+"\n got debited with amount "+
                amount+" "+currencyCode+"\n at "+time+
                "\nFinal amount in your wallet is "+finalAmount+" "+currencyCode
        );
        javaMailSender.send(msg);
    }


}
