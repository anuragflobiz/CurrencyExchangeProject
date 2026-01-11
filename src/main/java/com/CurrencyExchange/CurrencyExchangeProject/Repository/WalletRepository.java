package com.CurrencyExchange.CurrencyExchangeProject.Repository;

import com.CurrencyExchange.CurrencyExchangeProject.Entity.User;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.Wallet;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.CurrencyCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByIdAndUserEmailAndDeletedAtIsNull(UUID walletId, String email);
    List<Wallet> findAllByUserEmailAndDeletedAtIsNull(String email);
    Optional<Wallet> findByUserEmailAndCurrencyCodeAndDeletedAtIsNull(String email, CurrencyCode currencyCode);
}
