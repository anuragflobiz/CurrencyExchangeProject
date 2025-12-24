package com.CurrencyExchange.CurrencyExchangeProject.Controller;

import com.CurrencyExchange.CurrencyExchangeProject.Service.WalletService;
import com.CurrencyExchange.CurrencyExchangeProject.enums.CurrencyCode;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.WalletResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> createWallet(
            @RequestParam CurrencyCode currency,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.create(currency, authentication));
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(
            @PathVariable UUID walletId,
            Authentication authentication
    ) {
        walletService.deleteWallet(walletId, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WalletResponseDTO>> getAllWallet(@RequestParam(required = false) CurrencyCode currency, Authentication authentication){
        return ResponseEntity.ok(walletService.showWallets(currency,authentication));
    }

}