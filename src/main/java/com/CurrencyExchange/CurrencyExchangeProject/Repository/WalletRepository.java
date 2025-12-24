package com.CurrencyExchange.CurrencyExchangeProject.Repository;

import com.coinShiftProject.coinShiftProject.Entity.User;
import com.coinShiftProject.coinShiftProject.Entity.Wallet;
import com.coinShiftProject.coinShiftProject.enums.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
