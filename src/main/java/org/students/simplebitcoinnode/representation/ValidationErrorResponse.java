package org.students.simplebitcoinnode.representation;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ValidationErrorResponse extends BadRequestErrorResponse {
    private Map<String, String> invalidObjects;

    public ValidationErrorResponse(Map<String, String> invalidObjects) {
        super("Request body validation failed");
        this.invalidObjects = invalidObjects;
    }
}
