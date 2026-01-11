package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;



import com.CurrencyExchange.CurrencyExchangeProject.Entity.User;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.Wallet;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.CurrencyCode;
import com.CurrencyExchange.CurrencyExchangeProject.Exceptions.BadRequestException;
import com.CurrencyExchange.CurrencyExchangeProject.Exceptions.UnauthorizedAccessException;
import com.CurrencyExchange.CurrencyExchangeProject.Exceptions.UserNotFoundException;
import com.CurrencyExchange.CurrencyExchangeProject.Exceptions.WalletNotFoundException;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.UserRepository;
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

        return "Wallet created successfully";
    }

    @Override
        String email=authentication.getName();
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) != 0) {
        }
    }

    @Override
    public List<WalletResponseDTO> showWallets(CurrencyCode currencyCode, Authentication authentication) {
        String email = authentication.getName();

        if (currencyCode != null) {
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
            return List.of(
                    new WalletResponseDTO(wallet.getId(), wallet.getCurrencyCode(), wallet.getBalance())
            );
        }

                .map(w -> new WalletResponseDTO(w.getId(), w.getCurrencyCode(), w.getBalance()))
                .toList();
    }
}
