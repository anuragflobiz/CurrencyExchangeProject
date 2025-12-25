package com.CurrencyExchange.CurrencyExchangeProject.Service;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.RechargeWalletDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.SendMoneyDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.TransactionResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {

    public String sendMoney(SendMoneyDTO req, Authentication auth);

    public String rechargeWallet(RechargeWalletDTO req, Authentication authentication);

    public String convertCurrency(SendMoneyDTO req, Authentication authentication);

    public ResponseEntity<List<TransactionResponseDTO>> getAllTransaction(Authentication authentication);

}
