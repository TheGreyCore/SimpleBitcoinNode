package org.students.simplebitcoinnode.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjacentNodeDTO {
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
