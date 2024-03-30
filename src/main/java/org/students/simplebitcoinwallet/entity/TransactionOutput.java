package org.students.simplebitcoinwallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Table(name = "transaction_output")
public class TransactionOutput implements Serializable {
    @Id
    @Getter
    private Integer id;

    @Getter
    @Setter
    @Column(length = 128)
    @NotNull
    private String signature;

    @Getter
    @Setter
    private BigDecimal amount;

    @Getter
    @Setter
    private String receiverPublicKey;
}
