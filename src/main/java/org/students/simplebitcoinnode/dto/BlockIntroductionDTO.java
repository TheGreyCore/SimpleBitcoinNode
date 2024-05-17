package org.students.simplebitcoinnode.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockIntroductionDTO {
    private String previousHash;
    private MerkleTreeNodeDTO merkleTreeRoot;
    private LocalDateTime timestamp;
}
