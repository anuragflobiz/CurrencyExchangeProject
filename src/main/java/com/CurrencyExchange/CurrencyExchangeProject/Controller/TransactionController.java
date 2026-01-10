package com.CurrencyExchange.CurrencyExchangeProject.Controller;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.RechargeWalletDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.SendMoneyDTO;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.TransactionResponseDTO;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.UserRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/transfers")
    public ResponseEntity<String> transfer(@Valid @RequestBody SendMoneyDTO req, Authentication authentication){
        return ResponseEntity.ok(transactionService.sendMoney(req,authentication));
    }

    @PostMapping("/recharges")
    public ResponseEntity<String> recharge(@Valid @RequestBody RechargeWalletDTO req, Authentication authentication){
        return ResponseEntity.ok(transactionService.rechargeWallet(req,authentication));
    }

    @GetMapping
    public Page<TransactionResponseDTO> getMyTransactions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Authentication authentication) {
        return transactionService.getAllTransaction(authentication, page, size);
    }
}