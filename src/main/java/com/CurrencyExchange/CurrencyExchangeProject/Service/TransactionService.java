package com.CurrencyExchange.CurrencyExchangeProject.Service;

import com.coinShiftProject.coinShiftProject.DTO.TransactionResponseDTO;
import com.coinShiftProject.coinShiftProject.DTO.rechargeWalletDTO;
import com.coinShiftProject.coinShiftProject.DTO.sendMoneyDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {

    public String sendMoney(sendMoneyDTO req, Authentication auth);

    public String rechargeWallet(rechargeWalletDTO req, Authentication authentication);

    public String convertCurrency(sendMoneyDTO req, Authentication authentication);

    public ResponseEntity<List<TransactionResponseDTO>> getAllTransaction(Authentication authentication);

}
