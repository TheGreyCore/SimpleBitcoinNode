package org.students.simplebitcoinwallet.representation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ValidationErrorResponse extends BadRequestErrorResponse {
    @Setter
    @Getter
    private Map<String, String> invalidFields;
    public ValidationErrorResponse() {
        super("Request body validation failed");
    }

    public ValidationErrorResponse(Map<String, String> invalidFields) {
        super("Request body validation failed");
        this.invalidFields = invalidFields;
    }
}
