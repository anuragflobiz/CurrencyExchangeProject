package com.CurrencyExchange.CurrencyExchangeProject.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping
    public ResponseEntity<String> createWallet(@RequestParam CurrencyCode currency, Authentication authentication){
        return ResponseEntity.ok(walletService.create(currency,authentication));
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<String> deleteWallet(@PathVariable UUID walletId, Authentication authentication){
        return ResponseEntity.ok(walletService.deleteWallet(walletId,authentication));
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getAllWallet(@RequestParam(required = false) CurrencyCode currency, Authentication authentication){
        return ResponseEntity.ok(walletService.showWallets(currency,authentication));
    }

}