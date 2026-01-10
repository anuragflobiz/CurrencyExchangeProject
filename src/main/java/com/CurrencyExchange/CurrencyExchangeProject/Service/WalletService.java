package com.CurrencyExchange.CurrencyExchangeProject.Service;

import com.CurrencyExchange.CurrencyExchangeProject.DTO.WalletResponseDTO;
import com.CurrencyExchange.CurrencyExchangeProject.Enums.CurrencyCode;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface WalletService {
    public String create(CurrencyCode currencyCode, Authentication authentication);

    public String deleteWallet(UUID wallet_id, Authentication authentication);

    public List<WalletResponseDTO> showWallets(CurrencyCode currencyCode, Authentication authentication);

}