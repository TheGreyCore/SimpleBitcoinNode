package org.students.simplebitcoinnode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transaction_output")
public class TransactionOutput {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Column(length = 144)
    private String signature;

    @Setter
    private BigDecimal amount;

    @Setter
    @Column(length = 178)
    @NotNull(message = "Transaction output must specify receiver's public key and it cannot be left empty")
    private String receiverPublicKey;
}
