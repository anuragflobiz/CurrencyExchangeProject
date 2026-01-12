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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wallet wallet = new Wallet();
        wallet.setCurrencyCode(currencyCode);
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        try {
            walletRepository.save(wallet);
            return "Wallet created successfully";
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("User already has a wallet with this currency");
        }
    }


    @Override
    public String deleteWallet(UUID walletId, Authentication authentication) {

        String email = authentication.getName();
        Wallet wallet = walletRepository.findByIdAndUserEmailAndDeletedAtIsNull(walletId, email)
                .orElseThrow(() -> new UnauthorizedAccessException("Wallet not found or does not belong to you"));
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Wallet balance must be zero before deletion");
        }
        wallet.setDeletedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        return "Wallet deleted successfully";
    }


    @Override
    public List<WalletResponseDTO> showWallets(CurrencyCode currencyCode, Authentication authentication) {
        String email = authentication.getName();

        if (currencyCode != null) {
            Wallet wallet = walletRepository.findByUserEmailAndCurrencyCodeAndDeletedAtIsNull(email, currencyCode)
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
            return List.of(
                    new WalletResponseDTO(wallet.getId(), wallet.getCurrencyCode(), wallet.getBalance())
            );
        }

        return walletRepository
                .findAllByUserEmailAndDeletedAtIsNull(email)
                .stream()
                .map(w -> new WalletResponseDTO(w.getId(), w.getCurrencyCode(), w.getBalance()))
                .toList();
    }

}
