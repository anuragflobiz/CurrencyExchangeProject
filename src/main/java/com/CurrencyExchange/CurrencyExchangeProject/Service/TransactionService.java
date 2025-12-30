package com.CurrencyExchange.CurrencyExchangeProject.Service;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.RechargeWalletDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.SendMoneyDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.TransactionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {

    public String sendMoney(SendMoneyDTO req, Authentication auth);

    public String rechargeWallet(RechargeWalletDTO req, Authentication authentication);

    public Page<TransactionResponseDTO> getAllTransaction(Authentication authentication, int page, int size);

}
