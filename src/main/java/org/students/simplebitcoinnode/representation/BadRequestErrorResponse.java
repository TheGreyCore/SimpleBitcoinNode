package org.students.simplebitcoinnode.representation;

import org.springframework.http.HttpStatus;

public class BadRequestErrorResponse extends ErrorResponse {
    public BadRequestErrorResponse(String msg) {
        super(msg, HttpStatus.BAD_REQUEST.value());
    }
}
