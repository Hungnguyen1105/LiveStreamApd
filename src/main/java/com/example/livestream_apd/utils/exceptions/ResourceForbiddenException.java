package com.example.livestream_apd.utils.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//Không có quyền truy cập

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceForbiddenException extends RuntimeException {

    public ResourceForbiddenException(String message) {
        super(message);
    }
}
