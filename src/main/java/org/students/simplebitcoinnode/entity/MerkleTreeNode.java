package org.students.simplebitcoinnode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.util.Encoding;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intermediate_merkle_tree_nodes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class MerkleTreeNode implements Externalizable {
    @Serial
    private static final long serialVersionUID = 0x010000L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 64)
    @NotNull(message = "IntermediateMerkleTreeNode's hash cannot be set to null")
    @Length(min = 64, max = 64)
    private String hash;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_id")
    private List<MerkleTreeNode> children;

    // if transaction is specified then given tree node represents that transaction
    // if it's set to NULL then merkle tree node is intermediate
    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(children.size());
        try {
            for (MerkleTreeNode child : children) {
                out.write(Encoding.hexStringToBytes(child.getHash()));
            }
        }
        catch (InvalidEncodedStringException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        final int childrenCount = in.readInt();
        children = new ArrayList<>(childrenCount);

        // construct incomplete child objects
        for (int i = 0; i < childrenCount; i++) {
            byte[] hash = new byte[32];
            in.readFully(hash);
            children.add(MerkleTreeNode.builder().hash(Encoding.toHexString(hash)).build());
        }
    }
}
