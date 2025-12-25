package com.CurrencyExchange.CurrencyExchangeProject.Exceptions;

public class WalletNotFoundException extends RuntimeException{
    public WalletNotFoundException(String message) {

        super(message);
    }
}
