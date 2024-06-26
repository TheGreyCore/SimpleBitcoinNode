package org.students.simplebitcoinnode.dto;

import lombok.*;
import org.students.simplebitcoinnode.entity.validation.annotations.*;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.util.Encoding;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@CryptographicSignatureConstraint
@TransactionHashConstraint
@DoubleSpendingConstraint
@OneExternalRecipientPerTransactionConstraint
@MatchingInputReceiverAddressesConstraint
@TransactionInputSumIsOutputSumConstraint
public class TransactionDTO implements Externalizable {
    private String transactionHash = "";
    private List<TransactionOutputDTO> inputs = new ArrayList<>();

    @PositiveTransactionOutputConstraint
    private List<TransactionOutputDTO> outputs = new ArrayList<>();
    private String senderPublicKey = "";
    private LocalDateTime timestamp = LocalDateTime.now();

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            out.writeInt(inputs.size());
            for (TransactionOutputDTO input : inputs) {
                out.write(Encoding.hexStringToBytes(input.getSignature()));
                out.writeUTF(input.getAmount().toString());
                out.write(Encoding.defaultPubKeyDecoding(input.getReceiverPublicKey()));
            }
            out.writeInt(outputs.size());
            for (TransactionOutputDTO output : outputs) {
                out.writeUTF(output.getAmount().toString());
                out.write(Encoding.defaultPubKeyDecoding(output.getReceiverPublicKey()));
            }
            out.write(Encoding.defaultPubKeyDecoding(senderPublicKey));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
            out.writeUTF(timestamp.format(formatter));
        }
        catch (InvalidEncodedStringException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        transactionHash = in.readUTF();
        final int inputLength = in.readInt();
        inputs = new ArrayList<>(inputLength);
        for (int i = 0; i < inputLength; i++)
            inputs.set(i, (TransactionOutputDTO) in.readObject());
        final int outputLength = in.readInt();
        outputs = new ArrayList<>(outputLength);
        for (int i = 0; i < outputLength; i++)
            outputs.set(i, (TransactionOutputDTO) in.readObject());
    }
}
