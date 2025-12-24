package com.CurrencyExchange.CurrencyExchangeProject.Repository;

import com.coinShiftProject.coinShiftProject.DTO.TransactionResponseDTO;
import com.coinShiftProject.coinShiftProject.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,UUID>{

    @Query("""
        SELECT new com.coinShiftProject.coinShiftProject.DTO.TransactionResponseDTO(
            CASE
                WHEN t.senderUser.id = :id THEN t.receiverUser.name
                ELSE t.senderUser.name
            END,
            t.senderAmount,
            t.receiverAmount,
            t.paymentStatus,
            t.transactionType,
            t.senderWallet.currencyCode,
            t.receiverWallet.currencyCode,
            t.exchangeRate,
            t.fees,
            t.createdAt,
            CASE
                WHEN t.senderUser.id = :id THEN 'DEBIT'
                ELSE 'CREDIT'
            END
        )
    FROM Transaction t
    WHERE t.senderUser.id = :id
        OR t.receiverUser.id = :id
    ORDER BY t.createdAt DESC
    """)
    List<TransactionResponseDTO> findByUserId(UUID id);
}
