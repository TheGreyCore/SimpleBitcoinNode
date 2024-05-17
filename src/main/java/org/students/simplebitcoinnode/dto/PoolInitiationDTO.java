package org.students.simplebitcoinnode.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolInitiationDTO {
    private BlockIntroductionDTO block;
    private Integer expectedPoolSize;
}
