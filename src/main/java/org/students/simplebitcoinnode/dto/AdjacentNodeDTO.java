package org.students.simplebitcoinnode.dto;

import lombok.*;

import java.util.Optional;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjacentNodeDTO {
    private String name;
    private String hostname;
    private String bitcoinNodeProvider;
    private String version;
    private boolean tls = false;
    private boolean acceptPoolRequests = false;
    private float averageHashRate = 0.0f;
}
