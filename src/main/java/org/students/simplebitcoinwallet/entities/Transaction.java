package org.students.simplebitcoinwallet.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.students.simplebitcoinwallet.util.Encoding;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;

@Entity
@NoArgsConstructor
@Table(name = "ledger")
public class Transaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    @Column(length = 64)
    @NotNull(message = "Transaction hash (SHA256) must be previously calculated")
    private String transactionHash;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "input_id")
    @NotNull(message = "All transactions must have at least one input UTXO to use")
    private List<TransactionOutput> inputs;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "output_id")
    @NotNull(message = "All transactions must have at least one output")
    private List<TransactionOutput> outputs;

    @Getter
    @Setter
    @Column(length = 130)
    @NotNull(message = "Sender public key must be present in the transaction")
    private String senderPublicKey;

    @Getter
    @Setter
    private LocalDateTime timestamp = LocalDateTime.now(ZoneId.of("UTC"));
}
