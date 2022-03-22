package com.javeriana.shared.exceptions;

public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message, Object ...args) {
        super(String.format(message, args));
    }

}
