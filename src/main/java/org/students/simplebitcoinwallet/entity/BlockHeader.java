package org.students.simplebitcoinwallet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.students.simplebitcoinwallet.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinwallet.util.Encoding;

import java.io.*;
import java.math.BigInteger;

@Entity
@Table(name = "blocks")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockHeader implements Externalizable {
    @Serial
    private static final long serialVersionUID = 0x010000L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 64)
    @NotNull(message = "Previous hash (SHA256) must be previously calculated")
    @Length(min = 64, max = 64)
    private String previousHash;

    @Column(length = 64)
    @NotNull(message = "Merkle tree root must be specified in block header")
    @Length(min = 64, max = 64)
    private String merkleTreeRoot;

    @NotNull(message = "Nonce value cannot be null")
    private BigInteger nonce;

    @Column(length = 64)
    @NotNull(message = "Block hash cannot be set to null")
    private String hash;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            out.write(Encoding.hexStringToBytes(previousHash));
            out.write(Encoding.hexStringToBytes(merkleTreeRoot));
            byte[] nonceBytes = nonce.toByteArray();
            out.write(nonceBytes.length);
            out.write(nonceBytes);
        }
        catch (InvalidEncodedStringException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        // read previousArray and merkleTreeRoot hashes
        byte[] previousArray = new byte[32];
        byte[] merkleTreeRoot = new byte[32];

        in.readFully(previousArray);
        in.readFully(merkleTreeRoot);

        // read nonce value
        int nonceLength = in.readInt();
        byte[] nonce = new byte[nonceLength];
        in.readFully(nonce);
        this.nonce = new BigInteger(nonce);
    }
}
