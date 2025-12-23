package com.CurrencyExchange.CurrencyExchangeProject.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "mobile")
        }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = false, unique = true)
    @Setter
    private String email;

    @Column(nullable = false, unique = true)
    @Setter
    private String mobile;

    @Column(nullable = false)
    @Setter
    private String password; //store in hashed form

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wallet> walletList=new ArrayList<>();

    @OneToMany(mappedBy = "receiverUser")
    private List<Transaction> receiverTransactionList=new ArrayList<>();

    @OneToMany(mappedBy = "senderUser")
    private List<Transaction> senderTransactionList=new ArrayList<>();


}
