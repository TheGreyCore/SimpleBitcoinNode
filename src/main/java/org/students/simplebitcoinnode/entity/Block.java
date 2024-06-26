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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "BLOCKS")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block implements Externalizable, Cloneable {
    @Serial
    private static final long serialVersionUID = 0x010000L;

    // how many zero bits does the hash need to have in order for the block to be considered mined
    private static final long mineCondition = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    @NotNull(message = "Previous hash (SHA256) must be previously calculated")
    @Length(min = 64, max = 64)
    private String previousHash;

    @NotNull(message = "Each block must contain at least one transaction")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "merkle_tree_root", referencedColumnName = "id")
    private MerkleTreeNode merkleTree;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    private List<MinerPublicKey> miners;

    @Builder.Default
    private LocalDateTime blockAssemblyTimestamp = LocalDateTime.now(ZoneId.of("UTC"));

    // in case this is set to NULL, the block hasn't been mined yet
    private LocalDateTime minedTimestamp;

    @NotNull(message = "Nonce value cannot be null")
    @Column(scale = 64)
    private BigInteger nonce;

    @Column(length = 64)
    @NotNull(message = "Block hash cannot be set to null")
    private String hash;

    @Override
    public Object clone() {
        return new Block(id, previousHash, merkleTree, miners, blockAssemblyTimestamp, minedTimestamp, nonce, hash);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            out.write(Encoding.hexStringToBytes(previousHash));
            out.write(Encoding.hexStringToBytes(merkleTree.getHash()));
            for (MinerPublicKey miner : miners)
                out.write(Encoding.defaultPubKeyDecoding(miner.getPubKey()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
            out.writeUTF(blockAssemblyTimestamp.format(formatter));
            out.write(nonce.toByteArray());
        }
        catch (InvalidEncodedStringException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        // do nothing
    }
}
