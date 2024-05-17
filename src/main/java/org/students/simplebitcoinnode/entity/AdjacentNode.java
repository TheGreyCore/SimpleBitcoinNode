package org.students.simplebitcoinnode.entity;

import jakarta.persistence.*;
import lombok.*;
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
    private String ip;
    private String bitcoinNodeProvider;
    private String version;
    private String pubKey;
    private int port;
    private boolean tls;
    private boolean acceptPoolRequests;
    private float averageHashRate;
}
