package org.students.simplebitcoinnode.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.Optional;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjacentNodeDTO {
    @NotNull
    private String name;

    @NotNull
    private String hostname;

    @NotNull
    private String bitcoinNodeProvider;

    @NotNull
    private String version;

    @NotNull
    private String pubKey;

    @NotNull
    @Positive
    @Max(value = 65535, message = "Maximum port value is 65535")
    private int port;
    private boolean tls = false;
    private boolean acceptPoolRequests = false;

    @PositiveOrZero(message = "Average hash rate cannot be negative")
    private float averageHashRate = 0.0f;
}
