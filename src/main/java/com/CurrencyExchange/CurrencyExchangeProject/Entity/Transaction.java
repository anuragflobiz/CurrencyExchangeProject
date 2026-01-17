package com.CurrencyExchange.CurrencyExchangeProject.Entity;


import com.CurrencyExchange.CurrencyExchangeProject.Enums.PaymentStatus;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_sender_user", columnList = "sender_user_id"),
                @Index(name = "idx_tx_receiver_user", columnList = "receiver_user_id"),
                @Index(name = "idx_tx_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 4)
    @Setter
    private BigDecimal senderAmount;

    @Column(precision = 19, scale = 4)
    @Setter
    private BigDecimal receiverAmount;

    @Column(nullable = false, precision = 19, scale = 6)
    @Setter
    private BigDecimal exchangeRate;

    @Column(nullable = false, precision = 19, scale = 4)
    @Setter
    private BigDecimal fees;

    @Enumerated(EnumType.STRING)
    @Setter
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.INITIATED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_wallet_id",updatable = false)
    private Wallet senderWallet;

    @ManyToOne
    @JoinColumn(name = "receiver_wallet_id",updatable = false)
    private Wallet receiverWallet;


    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_user_id",updatable = false)
    private User senderUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_user_id",updatable = false)
    private User receiverUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
}
