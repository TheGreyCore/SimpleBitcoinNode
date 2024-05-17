package org.students.simplebitcoinnode.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerkleTreeNodeDTO {
    private String hash;
    private List<MerkleTreeNodeDTO> children;
    private TransactionDTO transaction;
}
