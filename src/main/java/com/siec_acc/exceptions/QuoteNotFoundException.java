package com.siec_acc.exceptions;

public class QuoteNotFoundException extends RuntimeException {
    public QuoteNotFoundException(String message) { super(message); }
}