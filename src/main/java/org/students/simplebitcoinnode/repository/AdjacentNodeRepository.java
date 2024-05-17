package org.students.simplebitcoinnode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.students.simplebitcoinnode.entity.AdjacentNode;

import java.util.List;

public interface AdjacentNodeRepository extends JpaRepository<AdjacentNode, Long> {
    /**
     * Finds best nodes by hash rate
     * @param maximumHashRate specifies the maximum hash rate one node can have
     * @param limit specifies the query limit
     * @return a list containing AdjacentNode objects
     */
    @Query(value = "SELECT * FROM ADJACENT_NODE WHERE AVERAGE_HASH_RATE < ?1 ORDER BY AVERAGE_HASH_RATE DESC LIMIT ?2", nativeQuery = true)
    List<AdjacentNode> findBestNodesByHashRate(Float maximumHashRate, Integer limit);
}
