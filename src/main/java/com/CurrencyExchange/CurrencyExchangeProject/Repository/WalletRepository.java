package com.CurrencyExchange.CurrencyExchangeProject.Repository;

import com.CurrencyExchange.CurrencyExchangeProject.Entity.Wallet;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.CurrencyCode;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("select w from Wallet w where w.id in (:senderId, :receiverId) and w.deletedAt is null")
    List<Wallet> findSendRecieverWalletAndDeletedAtIsNull(@Param("senderId") UUID senderId, @Param("receiverId") UUID receiverId);

    Optional<Wallet> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Wallet> findByIdAndUserEmailAndDeletedAtIsNull(UUID walletId, String email);
    List<Wallet> findAllByUserEmailAndDeletedAtIsNull(String email);
    Optional<Wallet> findByUserEmailAndCurrencyCodeAndDeletedAtIsNull(String email, CurrencyCode currencyCode);
}
