package com.example.integral_erp.exception;

public abstract class DomainException extends RuntimeException {

    public DomainException (String message) {
        super(message);
    }
}
