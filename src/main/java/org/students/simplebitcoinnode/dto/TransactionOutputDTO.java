package org.students.simplebitcoinnode.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionOutputDTO {
    @Builder.Default
    private String signature = "";

    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Builder.Default
    private String receiverPublicKey = "";
}
