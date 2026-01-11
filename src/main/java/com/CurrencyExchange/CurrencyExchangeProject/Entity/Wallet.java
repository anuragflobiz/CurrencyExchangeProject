package com.CurrencyExchange.CurrencyExchangeProject.Entity;

import com.CurrencyExchange.CurrencyExchangeProject.Enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id","currency_code","deleted_at"})
        },
        indexes = {
                @Index(name = "idx_wallet_user_id", columnList = "user_id")
        }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    @Setter
    private CurrencyCode currencyCode;

    @Column(nullable = false, precision = 19, scale = 4)
    @Setter
    private BigDecimal balance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @Setter
    private User user;

    @Version
    @Setter
    private Long version;

    @Setter
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "senderWallet")
    private List<Transaction> senderTransaction=new ArrayList<>();

    @OneToMany(mappedBy = "receiverWallet")
    private List<Transaction> receiverTransaction=new ArrayList<>();
}