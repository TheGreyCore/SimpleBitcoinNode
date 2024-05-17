package org.students.simplebitcoinnode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.students.simplebitcoinnode.entity.AdjacentNode;

public interface AdjacentNodeRepository extends JpaRepository<AdjacentNode, Integer> {
}
