package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;



import com.CurrencyExchange.CurrencyExchangeProject.Entity.User;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.Wallet;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.UserRepository;
import com.CurrencyExchange.CurrencyExchangeProject.enums.CurrencyCode;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.WalletResponseDTO;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.WalletRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;


    @Override
    public String create(CurrencyCode currencyCode, Authentication authentication) {
        String email = authentication.getName();
        User user= userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
        if(walletRepository.findByUserIdAndCurrencyCode(user.getId(),currencyCode).isPresent()){
            throw new RuntimeException("User already have wallet with this currency");
        }
        Wallet w=new Wallet();
        w.setCurrencyCode(currencyCode);
        w.setUser(user);
        w.setBalance(BigDecimal.ZERO);

        walletRepository.save(w);
        return "Wallet created successfully";
    }

    @Override
    public String deleteWallet(UUID wallet_id, Authentication authentication) {
        String email=authentication.getName();

        Wallet wallet = walletRepository.findById(wallet_id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Wallet does not belong to you");
        }

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("You have balance in your account before deletion you have to transfer all balance to some other wallet or any one else");
        }

        walletRepository.delete(wallet);
        return "wallet deleted successfully";
    }

    @Override
    public List<WalletResponseDTO> showWallets(CurrencyCode currencyCode, Authentication authentication) {
        String email = authentication.getName();
        User loggedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currencyCode != null) {
            Wallet wallet = walletRepository
                    .findByUserIdAndCurrencyCode(loggedUser.getId(), currencyCode)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            return List.of(
                    new WalletResponseDTO(wallet.getId(), wallet.getCurrencyCode(), wallet.getBalance())
            );
        }

        return walletRepository.findAllByUserId(loggedUser.getId()).stream()
                .map(w -> new WalletResponseDTO(w.getId(), w.getCurrencyCode(), w.getBalance()))
                .toList();
    }
}
