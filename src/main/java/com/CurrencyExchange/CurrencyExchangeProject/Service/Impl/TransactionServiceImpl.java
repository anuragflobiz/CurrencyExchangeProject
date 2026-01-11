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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
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


    public void handleSameCurrency(SendMoneyDTO dto, User sender, Wallet senderWallet, Wallet receiverWallet) {

        BigDecimal fee = SAME_CURRENCY_FEE;
        BigDecimal debit = dto.getSenderAmount().add(fee);

        if (senderWallet.getBalance().compareTo(debit) < 0) {
            throw new BadRequestException("Insufficient balance");
        }
        Transaction tx = Transaction.builder()
                .senderAmount(dto.getSenderAmount())
                .receiverAmount(dto.getSenderAmount())
                .exchangeRate(BigDecimal.ONE)
                .fees(fee)
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .senderUser(sender)
                .receiverUser(receiverWallet.getUser())
                .paymentStatus(PaymentStatus.INITIATED)
                .transactionType(TransactionType.TRANSFER)
                .build();
        transactionRepository.save(tx);
        senderWallet.setBalance(senderWallet.getBalance().subtract(debit));
        receiverWallet.setBalance(receiverWallet.getBalance().add(dto.getSenderAmount()));
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);
        tx.setPaymentStatus(PaymentStatus.SUCCESS);
        transactionRepository.save(tx);
        try{
            notificationProducer.send(new NotificationDTO(
                    "DEBIT",
                    sender.getEmail(),
                    debit,
                    senderWallet.getCurrencyCode().toString(),
                    tx.getCreatedAt(),
                    senderWallet.getBalance()
            ));
            notificationProducer.send(new NotificationDTO(
                    "CREDIT",
                    receiverWallet.getUser().getEmail(),
                    dto.getSenderAmount(),
                    receiverWallet.getCurrencyCode().toString(),
                    tx.getCreatedAt(),
                    receiverWallet.getBalance()
            ));
        }catch (Exception ex){
            log.warn("Failed to send notification for transactionId={}", tx.getId(), ex);
        }
    }

    public void handleDifferentCurrency(SendMoneyDTO req, User sender, Wallet senderWallet, Wallet receiverWallet) {

        String sendCurrency = senderWallet.getCurrencyCode().toString();
        String receiverCurrency = receiverWallet.getCurrencyCode().toString();
        String rate = redisTemplate.opsForValue()
                .get("RATE:" + sendCurrency + ":" + receiverCurrency);

        if (rate == null) {
            throw new ExchangeRateFetchException("Exchange rate not available");
        }

        BigDecimal exchangeRate = new BigDecimal(rate);
        BigDecimal fees = CROSS_CURRENCY_FEE;
        BigDecimal creditAmount = req.getSenderAmount().multiply(exchangeRate);
        BigDecimal debitAmount = req.getSenderAmount().add(fees);
        if (senderWallet.getBalance().compareTo(debitAmount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        User receiver = receiverWallet.getUser();
        Transaction tx = Transaction.builder()
                .senderAmount(req.getSenderAmount())
                .receiverAmount(creditAmount)
                .exchangeRate(exchangeRate)
                .fees(fees)
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .senderUser(sender)
                .receiverUser(receiver)
                .paymentStatus(PaymentStatus.INITIATED)
                .transactionType(
                        sender.getId().equals(receiver.getId())
                                ? TransactionType.CONVERSION
                                : TransactionType.TRANSFER
                )
                .build();

        transactionRepository.save(tx);
        senderWallet.setBalance(senderWallet.getBalance().subtract(debitAmount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(creditAmount));
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);
        tx.setPaymentStatus(PaymentStatus.SUCCESS);
        transactionRepository.save(tx);
        try{
            notificationProducer.send(new NotificationDTO(
                    "DEBIT",
                    sender.getEmail(),
                    debitAmount,
                    senderWallet.getCurrencyCode().toString(),
                    tx.getCreatedAt(),
                    senderWallet.getBalance()
            ));
            notificationProducer.send(new NotificationDTO(
                    "CREDIT",
                    receiver.getEmail(),
                    creditAmount,
                    receiverWallet.getCurrencyCode().toString(),
                    tx.getCreatedAt(),
                    receiverWallet.getBalance()
            ));
        }catch (Exception ex){
            log.warn("Failed to send notification for transactionId={}", tx.getId(), ex);
        }
    }

    @Override
    @Transactional
    public String rechargeWallet(RechargeWalletDTO req, Authentication authentication) {

        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid amount");
        }
        String email = authentication.getName();
        Wallet wallet = walletRepository
                .findByIdAndDeletedAtIsNull(req.getWalletid())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        if (!wallet.getUser().getEmail().equals(email)) {
            throw new UnauthorizedAccessException("Unauthorized wallet access");
        }
        BigDecimal amount = req.getAmount();
        Transaction tx = Transaction.builder()
                .senderAmount(amount)
                .receiverAmount(amount)
                .exchangeRate(BigDecimal.ONE)
                .fees(BigDecimal.ZERO)
                .senderWallet(wallet)
                .receiverWallet(wallet)
                .senderUser(wallet.getUser())
                .receiverUser(wallet.getUser())
                .paymentStatus(PaymentStatus.INITIATED)
                .transactionType(TransactionType.RECHARGE)
                .build();
        transactionRepository.save(tx);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        tx.setPaymentStatus(PaymentStatus.SUCCESS);
        transactionRepository.save(tx);
        try{
            notificationProducer.send(new NotificationDTO(
                    "CREDIT",
                    wallet.getUser().getEmail(),
                    amount,
                    wallet.getCurrencyCode().toString(),
                    tx.getCreatedAt(),
                    wallet.getBalance()
            ));
        }catch (Exception ex){
            log.warn("Failed to send notification for transactionId={}", tx.getId(), ex);
        }
        return "Wallet recharged successfully";
    }

    @Override
    public Page<TransactionResponseDTO> getAllTransaction(Authentication authentication, int page, int size) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return transactionRepository.findUserTransactions(user.getId(), pageable);
    }

    @Transactional
    @Override
    public String sendMoney(SendMoneyDTO req, Authentication auth) {
        String email=auth.getName();
        List<Wallet> wallets = walletRepository.findSendRecieverWalletAndDeletedAtIsNull(req.getFromWalletId(), req.getToWalletId());
        Wallet from = wallets.stream()
                .filter(w -> w.getId().equals(req.getFromWalletId()))
                .findFirst()
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));
        Wallet to = wallets.stream()
                .filter(w -> w.getId().equals(req.getToWalletId()))
                .findFirst()
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));
        if (!from.getUser().getEmail().equals(email)) {
            throw new UnauthorizedAccessException("Given sender wallet does not belong to you");
        }
        if (req.getSenderAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid amount");
        }
        if (from.getCurrencyCode().equals(to.getCurrencyCode())) {
            handleSameCurrency(req, from.getUser(), from, to);
        } else {
            handleDifferentCurrency(req, from.getUser(), from, to);
        }
        return "Transaction successful";
    }
}