package org.students.simplebitcoinnode.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockIntroductionDTO {
    private String previousHash;
    private MerkleTreeNodeDTO merkleTree;
    private LocalDateTime blockAssemblyTimestamp;
}
