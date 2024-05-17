package org.students.simplebitcoinnode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "MINER_PUBLIC_KEYS",
    uniqueConstraints = @UniqueConstraint(columnNames = {"PUB_KEY"})
)
public class MinerPublicKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 178)
    @NotNull(message = "Miner's public key must be specified")
    private String pubKey;
}
