package com.CurrencyExchange.CurrencyExchangeProject.Service;

import com.coinShiftProject.coinShiftProject.DTO.WalletResponse;
import com.coinShiftProject.coinShiftProject.enums.CurrencyCode;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface WalletService {
    public String create(CurrencyCode currencyCode, Authentication authentication);

    public String deleteWallet(UUID wallet_id, Authentication authentication);

    public List<WalletResponse> showWallets(CurrencyCode currencyCode, Authentication authentication);

}