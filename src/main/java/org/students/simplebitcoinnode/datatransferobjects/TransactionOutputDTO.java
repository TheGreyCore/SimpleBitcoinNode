package org.students.simplebitcoinnode.datatransferobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionOutputDTO {
    private String signature;
    private BigDecimal amount;
    private String receiverPublicKey;
}
