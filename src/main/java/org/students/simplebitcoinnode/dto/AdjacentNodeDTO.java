package org.students.simplebitcoinnode.dto;

import jakarta.validation.constraints.NotNull;
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
    private int port;
    private boolean tls = false;
    private boolean acceptPoolRequests = false;
    private float averageHashRate = 0.0f;
}
