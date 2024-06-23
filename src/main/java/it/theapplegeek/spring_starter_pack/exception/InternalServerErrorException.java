package it.theapplegeek.spring_starter_pack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String msg) {
        super(msg);
    }
}
