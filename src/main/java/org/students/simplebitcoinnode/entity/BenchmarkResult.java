package org.students.simplebitcoinnode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "benchmarkresult")
public class BenchmarkResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private float hashRate;
    private long sampleSize;
    private int threads;
    private LocalDateTime timestamp  = LocalDateTime.now(ZoneId.of("UTC"));
}
