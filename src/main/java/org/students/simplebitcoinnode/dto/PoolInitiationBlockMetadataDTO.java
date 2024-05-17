package org.students.simplebitcoinnode.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolInitiationBlockMetadataDTO {
    private List<String> miners;
    private Integer offset;
    private Integer stride;
}
