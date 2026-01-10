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
        t.receiverUser.name,
        t.senderAmount,
        t.receiverAmount,
        t.paymentStatus,
        t.transactionType,
        t.senderWallet.currencyCode,
        t.receiverWallet.currencyCode,
        t.exchangeRate,
        t.fees,
        t.createdAt,
        'DEBIT'
    )
    FROM Transaction t
    WHERE t.senderUser.id = :id
    """)
    Page<TransactionResponseDTO> findDebitTransactions(UUID id, Pageable pageable);

    @Query("""
    SELECT new com.CurrencyExchange.CurrencyExchangeProject.DTO.TransactionResponseDTO(
        t.senderUser.name,
        t.senderAmount,
        t.receiverAmount,
        t.paymentStatus,
        t.transactionType,
        t.senderWallet.currencyCode,
        t.receiverWallet.currencyCode,
        t.exchangeRate,
        t.fees,
        t.createdAt,
        'CREDIT'
    )
    FROM Transaction t
    WHERE t.receiverUser.id = :id AND t.senderUser.id !=t.receiverUser.id
    """)
    Page<TransactionResponseDTO> findCreditTransactions(UUID id, Pageable pageable);
}
