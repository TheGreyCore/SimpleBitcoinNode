package org.students.simplebitcoinnode.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Setter
@Getter
@Builder
@NotNull
public class DistributedMiningRequestDTO {
    private String previousHash;
    private String merkleTreeRoot;
    private List<TransactionDTO> merkleTreeLeaves;
    private LocalDateTime blockAssemblyTimestamp;
    private int nodesInPool;
    private int offset;
}
