package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;


import com.coinShiftProject.coinShiftProject.DTO.WalletResponse;
import com.coinShiftProject.coinShiftProject.Entity.User;
import com.coinShiftProject.coinShiftProject.Entity.Wallet;
import com.coinShiftProject.coinShiftProject.Repository.UserRepository;
import com.coinShiftProject.coinShiftProject.Repository.WalletRepository;
import com.coinShiftProject.coinShiftProject.Service.WalletService;
import com.coinShiftProject.coinShiftProject.enums.CurrencyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public String create(CurrencyCode currencyCode, Authentication authentication) {
        String email=authentication.getPrincipal().toString();
        User user= (User) userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
        if(walletRepository.findByUserIdAndCurrencyCode(user.getId(),currencyCode).isPresent()){
            throw new RuntimeException("User already have wallet with this currency");
        }
        Wallet w=new Wallet();
        w.setCurrencyCode(currencyCode);
        w.setUser(user);

        walletRepository.save(w);
        return "Wallet created successfully";
    }

    @Override
    public String deleteWallet(UUID wallet_id, Authentication authentication) {
        String email=authentication.getName();

        User user =walletRepository.findUserByWalletId(wallet_id);
        if(!user.getEmail().equals(email)){
            throw new RuntimeException("Given wallet does not belongs to you");
        }

        Wallet wallet=walletRepository.findById(wallet_id).orElseThrow(()->new
                RuntimeException("Wallet does not found with given Id"));

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("You have balance in your account before deletion you have to transfer all balance to some other wallet or any one else");
        }

        walletRepository.deleteById(wallet_id);
        return "wallet deleted successfully";
    }

    @Override
    public List<WalletResponse> showWallets(CurrencyCode currencyCode, Authentication authentication) {
        String email = authentication.getName();
        User loggedUser = (User) userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currencyCode != null) {
            Wallet wallet = walletRepository
                    .findByUserIdAndCurrencyCode(loggedUser.getId(), currencyCode)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            return List.of(
                    new WalletResponse(wallet.getId(), wallet.getCurrencyCode(), wallet.getBalance())
            );
        }

        return walletRepository.findAllByUserId(loggedUser.getId()).stream()
                .map(w -> new WalletResponse(w.getId(), w.getCurrencyCode(), w.getBalance()))
                .toList();
    }
}
