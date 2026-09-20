package org.natwest.account.exception;

public class TransactionClientException extends RuntimeException{

    public TransactionClientException(String message) {
        super(message);
    }
    public TransactionClientException( String message, Throwable cause) {
        super(message, cause);
    }
}
