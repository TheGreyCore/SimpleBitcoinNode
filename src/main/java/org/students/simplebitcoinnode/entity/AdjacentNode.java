package org.students.simplebitcoinnode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "adjacentNode")
public class AdjacentNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String hostname;

    @Column(length = 39)
    private String ip;
    private String bitcoinNodeProvider;
    private String version;

    @Column(length = 178)
    private String pubKey;
    private int port;
    private boolean tls;
    private boolean acceptPoolRequests;
    private float averageHashRate;
}
