package org.students.simplebitcoinwallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transaction_output")
public class TransactionOutput implements Serializable {
    @Id
    @Getter
    private Integer id;

    @Getter
    @Setter
    @Column(length = 143)
    private String signature;

    @Getter
    @Setter
    private BigDecimal amount;

    @Getter
    @Setter
    @Column(length = 177)
    @NotNull(message = "Transaction output must specify receiver's public key and it cannot be left empty")
    private String receiverPublicKey;
}
