package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.*;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.Transaction;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.User;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.Wallet;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.TransactionRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.UserRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.WalletRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Service.TransactionService;
import com.CurrencyExchange.CurrencyExchangeProject.enums.PaymentStatus;
import com.CurrencyExchange.CurrencyExchangeProject.enums.TransactionType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final BigDecimal SAME_CURRENCY_FEE = BigDecimal.TEN;
    private static final BigDecimal CROSS_CURRENCY_FEE = BigDecimal.valueOf(50);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private NotificationProducer notificationProducer;


    public void handleSameCurrency(SendMoneyDTO sendMoneyDTO,User sender,Wallet senderWallet,Wallet receiverWallet ) {

        BigDecimal fees =SAME_CURRENCY_FEE;
        BigDecimal debit =fees.add(sendMoneyDTO.getSenderAmount());
        if(senderWallet.getBalance().compareTo(debit)<0){
            throw new RuntimeException("Insufficient balance");
        }

        Transaction tx = Transaction.builder()
                .senderAmount(sendMoneyDTO.getSenderAmount())
                .receiverAmount(sendMoneyDTO.getSenderAmount())
                .exchangeRate(BigDecimal.ONE)
                .fees(fees)
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .senderUser(sender)
                .receiverUser(receiverWallet.getUser())
                .paymentStatus(PaymentStatus.INITIATED)
                .transactionType(TransactionType.TRANSFER)
                .build();

        transactionRepository.save(tx);

        try {
            senderWallet.setBalance(senderWallet.getBalance().subtract(debit));
            receiverWallet.setBalance(receiverWallet.getBalance().add(sendMoneyDTO.getSenderAmount()));
            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);
            tx.setPaymentStatus(PaymentStatus.SUCCESS);
            transactionRepository.save(tx);
            notificationProducer.send(new NotificationDTO("DEBIT", sender.getEmail(), debit,
                    senderWallet.getCurrencyCode().toString(), tx.getCreatedAt(),
                    senderWallet.getBalance())
            );
            notificationProducer.send(new NotificationDTO("CREDIT",
                    receiverWallet.getUser().getEmail(), sendMoneyDTO.getSenderAmount(),
                    receiverWallet.getCurrencyCode().toString(), tx.getCreatedAt(),
                    receiverWallet.getBalance())
            );
        }catch (Exception e) {
            tx.setPaymentStatus(PaymentStatus.FAILED);
            transactionRepository.save(tx);
            throw e;
        }
    }

    public void handleDifferentCurrency(SendMoneyDTO req,User sender,Wallet senderWallet,Wallet receiverWallet){
        String sendCurrency=senderWallet.getCurrencyCode().toString();
        String recieverCurrency=receiverWallet.getCurrencyCode().toString();

        String rate = redisTemplate.opsForValue()
                .get("RATE:"+sendCurrency+":"+recieverCurrency);

        if (rate == null) {
            throw new RuntimeException("Exchange rate not available");
        }

        BigDecimal exchangeRate = new BigDecimal(rate);


        BigDecimal fees=CROSS_CURRENCY_FEE;
        BigDecimal creditAmount =req.getSenderAmount().multiply(exchangeRate);
        BigDecimal debitAmount =req.getSenderAmount().add(fees);
        if(senderWallet.getBalance().compareTo(debitAmount)<0){
            throw new RuntimeException("Insufficient balance");
        }

        Transaction tx = Transaction.builder()
                .senderAmount(req.getSenderAmount())
                .receiverAmount(creditAmount)
                .exchangeRate(exchangeRate)
                .fees(fees)
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .senderUser(sender)
                .receiverUser(receiverWallet.getUser())
                .paymentStatus(PaymentStatus.INITIATED)
                .transactionType(TransactionType.TRANSFER)
                .build();
        transactionRepository.save(tx);

        try {
            senderWallet.setBalance(senderWallet.getBalance().subtract(debitAmount));
            receiverWallet.setBalance(receiverWallet.getBalance().add(creditAmount));
            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);
            tx.setPaymentStatus(PaymentStatus.SUCCESS);
            transactionRepository.save(tx);
            notificationProducer.send(new NotificationDTO("DEBIT",sender.getEmail(),debitAmount,
                    senderWallet.getCurrencyCode().toString(),
                    tx.getCreatedAt(),senderWallet.getBalance())
            );

            notificationProducer.send(new NotificationDTO("CREDIT",
                    receiverWallet.getUser().getEmail(),creditAmount,
                    receiverWallet.getCurrencyCode().toString(),tx.getCreatedAt(),
                    receiverWallet.getBalance())
            );
        }catch (Exception e) {
            tx.setPaymentStatus(PaymentStatus.FAILED);
            transactionRepository.save(tx);
            throw e;
        }
    }

    @Override
    @Transactional
    public String rechargeWallet(RechargeWalletDTO req,Authentication authentication){

        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        String email=authentication.getName();
        System.out.println(req.getWalletid());
        Wallet wallet=walletRepository.findById(req.getWalletid()).orElseThrow(()->new RuntimeException("Wallet Does not found"));
        User user= (User) userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
        if (!wallet.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized wallet access");
        }

        BigDecimal finalamount=req.getAmount();

        Transaction tx = Transaction.builder()
                .senderAmount(finalamount)
                .receiverAmount(finalamount)
                .exchangeRate(BigDecimal.ONE)
                .fees(BigDecimal.ZERO)
                .senderWallet(wallet)
                .receiverWallet(wallet)
                .senderUser(user)
                .receiverUser(user)
                .paymentStatus(PaymentStatus.INITIATED)
                .transactionType(TransactionType.RECHARGE)
                .build();

        transactionRepository.save(tx);
        try{
            wallet.setBalance(
                    wallet.getBalance().add(finalamount)
            );
            walletRepository.save(wallet);
            tx.setPaymentStatus(PaymentStatus.SUCCESS);
            notificationProducer.send(new NotificationDTO("CREDIT",
                    email, finalamount,
                    wallet.getCurrencyCode().toString(), tx.getCreatedAt(),
                    wallet.getBalance())
            );
            return "Wallet recharged successfully";

        }catch (Exception e) {
            tx.setPaymentStatus(PaymentStatus.FAILED);
            transactionRepository.save(tx);
            throw e;
        }


    }

    @Override
    @Transactional
    public String convertCurrency(SendMoneyDTO req, Authentication authentication){
        String email=authentication.getName();

        User user=(User) userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
        Wallet sendWallet=walletRepository.findById(req.getFromWalletId()).orElseThrow(()->new RuntimeException("Sender wallet not found"));
        Wallet recieverWallet=walletRepository.findById(req.getToWalletId()).orElseThrow(()->new RuntimeException("Reciever wallet not found"));
        if(sendWallet.getCurrencyCode().equals(recieverWallet.getCurrencyCode())){
            throw new RuntimeException("Both wallets have same currency, conversion not required");
        }

        if (!sendWallet.getUser().getId().equals(user.getId()) ||
                !recieverWallet.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized wallet access");
        }

        BigDecimal senderAmount = req.getSenderAmount();
        if (senderAmount == null || senderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        if(sendWallet.getBalance().compareTo(senderAmount)<0){
            throw new RuntimeException("Insufficient Balance");
        }

        String sendCurrency=sendWallet.getCurrencyCode().toString();
        String recieverCurrency=recieverWallet.getCurrencyCode().toString();
        String rate = redisTemplate.opsForValue()
                .get("RATE:"+sendCurrency+":"+recieverCurrency);

        if (rate == null) {
            throw new RuntimeException("Exchange rate not available");
        }

        BigDecimal exchangeRate = new BigDecimal(rate);
        BigDecimal recieverAmount=exchangeRate.multiply(senderAmount);

        Transaction tx = Transaction.builder()
                .senderAmount(senderAmount)
                .receiverAmount(recieverAmount)
                .exchangeRate(exchangeRate)
                .fees(BigDecimal.ZERO)
                .senderWallet(sendWallet)
                .receiverWallet(recieverWallet)
                .senderUser(user)
                .receiverUser(user)
                .paymentStatus(PaymentStatus.INITIATED)
                .transactionType(TransactionType.CONVERSION)
                .build();
        transactionRepository.save(tx);

        try{
            sendWallet.setBalance(sendWallet.getBalance().subtract(senderAmount));
            recieverWallet.setBalance(recieverWallet.getBalance().add(recieverAmount));
            walletRepository.save(sendWallet);
            walletRepository.save(recieverWallet);
            tx.setPaymentStatus(PaymentStatus.SUCCESS);
            transactionRepository.save(tx);
            notificationProducer.send(new NotificationDTO("DEBIT",email,senderAmount,
                    sendCurrency, tx.getCreatedAt(),sendWallet.getBalance())
            );

            notificationProducer.send(new NotificationDTO("CREDIT",
                    email,recieverAmount, recieverCurrency,tx.getCreatedAt(), recieverWallet.getBalance())
            );

            return "Your Amount get Converted";
        }catch (Exception e) {
            tx.setPaymentStatus(PaymentStatus.FAILED);
            transactionRepository.save(tx);
            throw e;
        }
    }

    @Override
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransaction(Authentication auth) {

        User user = (User) userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UUID userId=user.getId();
        List<TransactionResponseDTO> txs =
                transactionRepository.findByUserId(userId);

        return txs.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(txs);
    }

    @Transactional
    public String sendMoney(SendMoneyDTO req, Authentication auth) {

        String email=auth.getName();
        User sender= userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));

        Wallet from = walletRepository.findById(req.getFromWalletId())
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));
        Wallet to = walletRepository.findById(req.getToWalletId())
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

        if(!sender.getId().equals(from.getUser().getId())){
            throw new RuntimeException("Given sender wallet does not belongs to you");
        }

        if (from.getCurrencyCode().equals(to.getCurrencyCode())) {
            handleSameCurrency(req, sender, from, to);
        } else {
            handleDifferentCurrency(req, sender, from, to);
        }

        return "Transaction successful";
    }
}

