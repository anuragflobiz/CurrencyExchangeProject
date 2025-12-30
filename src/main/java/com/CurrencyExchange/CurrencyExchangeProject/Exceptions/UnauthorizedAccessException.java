package com.CurrencyExchange.CurrencyExchangeProject.Exceptions;

public class UnauthorizedAccessException extends RuntimeException{

    public UnauthorizedAccessException(String message) {

        super(message);
    }
}
