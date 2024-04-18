package org.students.simplebitcoinnode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;

public interface MerkleTreeNodeRepository extends JpaRepository<MerkleTreeNode, Integer> {

}
