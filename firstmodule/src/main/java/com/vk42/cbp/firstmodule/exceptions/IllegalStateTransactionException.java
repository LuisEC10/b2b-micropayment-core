package com.vk42.cbp.firstmodule.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalStateTransactionException extends RuntimeException {
    public IllegalStateTransactionException(String message) {
        super(message);
    }
}
