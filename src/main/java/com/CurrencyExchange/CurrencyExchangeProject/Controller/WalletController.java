package com.CurrencyExchange.CurrencyExchangeProject.Controller;

import com.coinShiftProject.coinShiftProject.DTO.WalletResponse;
import com.coinShiftProject.coinShiftProject.Service.AuthService;
import com.coinShiftProject.coinShiftProject.Service.WalletService;
import com.coinShiftProject.coinShiftProject.enums.CurrencyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private AuthService authService;

    @PostMapping("/create/{currency}")
    public ResponseEntity<String> createWallet(@PathVariable CurrencyCode currency, Authentication authentication){
        return ResponseEntity.ok(walletService.create(currency,authentication));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteWallet(@PathVariable UUID id, Authentication authentication){
        return ResponseEntity.ok(walletService.deleteWallet(id,authentication));
    }

    @GetMapping({"/showWallets", "/showWallets/{currency}"})
    public ResponseEntity<List<WalletResponse>> getAllWallet(@PathVariable(required = false) CurrencyCode currency, Authentication authentication){
        return ResponseEntity.ok(walletService.showWallets(currency,authentication));
    }

}