package com.CurrencyExchange.CurrencyExchangeProject.Repository;


import com.CurrencyExchange.CurrencyExchangeProject.Entity.Transaction;
import com.CurrencyExchange.CurrencyExchangeProject.DTO.TransactionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,UUID>{

    @Query("""
    SELECT new com.CurrencyExchange.CurrencyExchangeProject.DTO.TransactionResponseDTO(
        CASE 
            WHEN t.senderUser.id = :userId THEN t.receiverUser.name
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
            WHEN t.senderUser.id = :userId THEN 'DEBIT'
            ELSE 'CREDIT'
        END
    )
    FROM Transaction t
    WHERE t.senderUser.id = :userId
       OR t.receiverUser.id = :userId
    """)
    Page<TransactionResponseDTO> findUserTransactions(UUID userId, Pageable pageable);


}
