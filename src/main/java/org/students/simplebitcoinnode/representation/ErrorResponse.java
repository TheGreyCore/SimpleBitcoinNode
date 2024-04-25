package org.students.simplebitcoinnode.representation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ErrorResponse {
    @Getter
    @Setter
    protected String errorMessage;

    @Getter
    @Setter
    protected Integer httpCode;
}
