package org.students.simplebitcoinnode.representation;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class ValidationErrorResponse extends BadRequestErrorResponse {
    @Setter
    @Getter
    private Map<String, String> invalidObjects;
    public ValidationErrorResponse() {
        super("Request body validation failed");
    }

    public ValidationErrorResponse(Map<String, String> invalidObjects) {
        super("Request body validation failed");
        this.invalidObjects = invalidObjects;
    }
}
