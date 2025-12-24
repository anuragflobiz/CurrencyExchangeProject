package com.CurrencyExchange.CurrencyExchangeProject.Controller;

import com.coinShiftProject.coinShiftProject.DTO.TransactionResponseDTO;
import com.coinShiftProject.coinShiftProject.DTO.rechargeWalletDTO;
import com.coinShiftProject.coinShiftProject.DTO.sendMoneyDTO;
import com.coinShiftProject.coinShiftProject.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;


    @PostMapping("/sendMoney")
    public ResponseEntity<String> transferInDiffCurrency(@RequestBody sendMoneyDTO req, Authentication authentication){
        return ResponseEntity.ok(transactionService.sendMoney(req,authentication));
    }

    @PostMapping("/rechargeWallet")
    public ResponseEntity<String> rechargeWallet(@RequestBody rechargeWalletDTO req, Authentication authentication){
        return ResponseEntity.ok(transactionService.rechargeWallet(req,authentication));
    }

    @PostMapping("/convertCurrency")
    public ResponseEntity<String>convertCurrency(@RequestBody sendMoneyDTO req,Authentication authentication){
        return ResponseEntity.ok(transactionService.convertCurrency(req,authentication));
    }

    @GetMapping("/getMyTransaction")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransaction(Authentication authentication){
        return transactionService.getAllTransaction(authentication);
    }



}
