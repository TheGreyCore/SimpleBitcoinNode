package org.students.simplebitcoinnode.dto;

import lombok.*;
import org.students.simplebitcoinnode.entity.validation.annotations.PoolMiningProposalDTOConstraint;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PoolMiningProposalDTOConstraint
public class PoolMiningProposalDTO {
    private BlockIntroductionDTO block;
    private Integer expectedPoolSize;
}
