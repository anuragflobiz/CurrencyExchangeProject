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

    @Query(""" 
           select w.user from Wallet w where w.id=:id""")
    User findUserByWalletId(UUID id);

    Optional<Wallet> findByUserIdAndCurrencyCode(UUID id, CurrencyCode currencyCode);
    List<Wallet> findAllByUserId(UUID id);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select w from Wallet w
        where w.id = :senderId or w.id = :recieverId
    """)
    List<Wallet> findSendRecieverWallet(UUID senderId, UUID recieverId);
}
