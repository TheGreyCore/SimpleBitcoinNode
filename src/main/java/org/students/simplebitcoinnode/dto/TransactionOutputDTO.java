package org.students.simplebitcoinnode.dto;

import lombok.*;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.util.Encoding;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionOutputDTO implements Externalizable {
    @Builder.Default
    private String signature = "";

    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Builder.Default
    private String receiverPublicKey = "";

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            byte[] sigBytes = Encoding.hexStringToBytes(signature);
            out.writeInt(sigBytes.length);
            out.write(sigBytes);
            out.writeObject(amount);
            byte[] pubKeyBytes = Encoding.defaultPubKeyDecoding(receiverPublicKey);
            out.writeInt(pubKeyBytes.length);
            out.write(pubKeyBytes);
        }
        catch (InvalidEncodedStringException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        final int sigLen = in.readInt();
        byte[] sigBytes = new byte[sigLen];
        in.read(sigBytes);
        signature = Encoding.toHexString(sigBytes);

        amount = (BigDecimal) in.readObject();

        final int pubKeyLen = in.readInt();
        byte[] pubKeyBytes = new byte[pubKeyLen];
        in.read(pubKeyBytes);
        receiverPublicKey = Encoding.defaultPubKeyEncoding(pubKeyBytes);
    }
}
