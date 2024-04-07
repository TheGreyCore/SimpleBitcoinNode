package org.students.simplebitcoinwallet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ledger")
//@TransactionHashConstraint
//@DoubleSpendingConstraint
//@CryptographicSignatureConstraint
public class Transaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 64)
    @NotNull(message = "Transaction hash (SHA256) must be previously calculated")
    private String transactionHash;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "input_id")
    @NotNull(message = "All transactions must have at least one input UTXO to use")
    private List<TransactionOutput> inputs;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "output_id")
    @NotNull(message = "All transactions must have at least one output")
    private List<TransactionOutput> outputs;

    @Column(length = 178)
    @NotNull(message = "Sender public key must be present in the transaction")
    private String senderPublicKey;

    private LocalDateTime timestamp = LocalDateTime.now(ZoneId.of("UTC"));
}
