package org.students.simplebitcoinnode.dto;

import lombok.*;
import org.students.simplebitcoinnode.entity.validation.annotations.PoolInitiationBlockMetadataConstraint;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PoolInitiationBlockMetadataConstraint
public class PoolInitiationBlockMetadataDTO {
    private String hash;
    private List<String> miners;
    private Integer offset;
}
