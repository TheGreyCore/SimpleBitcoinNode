package org.students.simplebitcoinnode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.util.Encoding;

import java.io.*;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "BLOCKS")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockHeader implements Externalizable {
    @Serial
    private static final long serialVersionUID = 0x010000L;

    // how many zero bits does the hash need to have in order for the block to be considered mined
    private static final long mineCondition = 8;

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

    @Builder.Default
    private LocalDateTime blockAssemblyTimestamp = LocalDateTime.now(ZoneId.of("UTC"));

    // in case this is set to NULL, the block hasn't been mined yet
    private LocalDateTime minedTimestamp;

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
            out.writeObject(blockAssemblyTimestamp);
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
