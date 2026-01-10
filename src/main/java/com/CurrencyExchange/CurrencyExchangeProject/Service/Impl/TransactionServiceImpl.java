package com.CurrencyExchange.CurrencyExchangeProject.Service.Impl;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.*;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.Transaction;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.User;
import com.CurrencyExchange.CurrencyExchangeProject.Entity.Wallet;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.PaymentStatus;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.TransactionType;
import com.CurrencyExchange.CurrencyExchangeProject.Exceptions.*;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.TransactionRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.UserRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Repository.WalletRepository;
import com.CurrencyExchange.CurrencyExchangeProject.Service.TransactionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
            throw new BadRequestException("Insufficient balance");
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
            throw new ExchangeRateFetchException("Exchange rate not available");
        }

        BigDecimal exchangeRate = new BigDecimal(rate);


        BigDecimal fees=CROSS_CURRENCY_FEE;
        BigDecimal creditAmount =req.getSenderAmount().multiply(exchangeRate);
        BigDecimal debitAmount =req.getSenderAmount().add(fees);
        if(senderWallet.getBalance().compareTo(debitAmount)<0){
            throw new ExchangeRateFetchException("Insufficient balance");
        }

        User reciever=receiverWallet.getUser();

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
                .transactionType((sender.getId().equals(reciever.getId()))?TransactionType.CONVERSION:TransactionType.TRANSFER)
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
            throw new BadRequestException("Invalid amount");
        }

        String email=authentication.getName();
        System.out.println(req.getWalletid());
        Wallet wallet=walletRepository.findById(req.getWalletid()).orElseThrow(()->new WalletNotFoundException("Wallet Does not found"));
        User user= userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("User not found"));
        if (!wallet.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Unauthorized wallet access");
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
    public Page<TransactionResponseDTO>getAllTransaction(Authentication authentication,int page,int size){
        String email=authentication.getName();

        User user=userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("User not found"));
        UUID userId=user.getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TransactionResponseDTO> debit = transactionRepository.findDebitTransactions(userId,pageable);
        Page<TransactionResponseDTO> credit=transactionRepository.findCreditTransactions(userId,pageable);

        List<TransactionResponseDTO> mergedList = new ArrayList<>();
        mergedList.addAll(credit.getContent());
        mergedList.addAll(debit.getContent());
        mergedList.sort(Comparator.comparing(TransactionResponseDTO::getCreatedAt).reversed());

        int start=page*size;
        int end=Math.min(start+size,mergedList.size());
        List<TransactionResponseDTO> pageContent = start >= mergedList.size() ? List.of() : mergedList.subList(start, end);

        return new PageImpl<>(pageContent,pageable,mergedList.size());

    }

    @Transactional
    public String sendMoney(SendMoneyDTO req, Authentication auth) {

        String email=auth.getName();
        User sender=userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("Sender not found"));
        List<Wallet> wallets = walletRepository.findSendRecieverWallet(req.getFromWalletId(), req.getToWalletId());

        Wallet from = wallets.stream()
                .filter(w -> w.getId().equals(req.getFromWalletId()))
                .findFirst()
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));

        Wallet to = wallets.stream()
                .filter(w -> w.getId().equals(req.getToWalletId()))
                .findFirst()
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));

        if (!from.getUser().getId().equals(sender.getId())) {
            throw new UnauthorizedAccessException("Given sender wallet does not belong to you");
        }

        if (from.getCurrencyCode().equals(to.getCurrencyCode())) {
            handleSameCurrency(req, from.getUser(), from, to);
        } else {
            handleDifferentCurrency(req, from.getUser(), from, to);
        }
        return "Transaction successful";
    }
}