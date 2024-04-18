package org.students.simplebitcoinwallet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.students.simplebitcoinwallet.entity.validation.annotations.CryptographicSignatureConstraint;
import org.students.simplebitcoinwallet.entity.validation.annotations.DoubleSpendingConstraint;
import org.students.simplebitcoinwallet.entity.validation.annotations.TransactionHashConstraint;
import org.students.simplebitcoinwallet.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinwallet.util.Encoding;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ledger")

// These are the problematic ConstraintValidator annotations which cause problems when saving transactions into JpaRepository
@TransactionHashConstraint
@DoubleSpendingConstraint
@CryptographicSignatureConstraint
public class Transaction implements Externalizable {
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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            out.writeInt(inputs.size());
            for (TransactionOutput input : inputs) {
                out.write(Encoding.hexStringToBytes(input.getSignature()));
                out.writeObject(input.getAmount());
                out.write(Encoding.defaultPubKeyDecoding(input.getReceiverPublicKey()));
            }
            out.writeInt(outputs.size());
            for (TransactionOutput output : outputs) {
                out.writeObject(output.getAmount());
                out.write(Encoding.defaultPubKeyDecoding(output.getReceiverPublicKey()));
            }
            out.write(Encoding.defaultPubKeyDecoding(senderPublicKey));
            out.writeObject(timestamp);
        }
        catch (InvalidEncodedStringException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // read transaction inputs and outputs
        inputs = new ArrayList<>(in.readInt());
        readTransactionOutputs(inputs, in);
        outputs = new ArrayList<>(in.readInt());
        readTransactionOutputs(outputs, in);

        // read sender public key and timestamp
        byte[] senderPublicKey = new byte[88];
        in.read(senderPublicKey);
        this.senderPublicKey = Encoding.defaultPubKeyEncoding(senderPublicKey);
        this.timestamp = (LocalDateTime) in.readObject();
    }

    private void readTransactionOutputs(List<TransactionOutput> transactionOutputs, ObjectInput in) throws IOException, ClassNotFoundException {
        for (int i = 0; i < transactionOutputs.size(); i++) {
            // deserialize input data
            byte[] signature = new byte[72];
            in.read(signature);
            BigDecimal amount = (BigDecimal) in.readObject();
            byte[] receiverPublicKey = new byte[88];
            in.read(receiverPublicKey);

            // save deserialized data into transaction input
            inputs.add(TransactionOutput.builder()
                .signature(Encoding.toHexString(signature))
                .amount(amount)
                .receiverPublicKey(Encoding.defaultPubKeyEncoding(receiverPublicKey))
                .build()
            );
        }
    }
}