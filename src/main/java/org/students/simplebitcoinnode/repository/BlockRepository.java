package org.students.simplebitcoinnode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    /**
     * Attempts to find a block header by specified hash
     * @param hash specifies the hash to use for block query
     * @return Optional wrapper object containing BlockHeader if query returned a BlockHeader, otherwise the wrapper contains null value
     */
    Optional<Block> findBlockHeaderByHash(String hash);

    /**
     * Attempts to find a next block that is chained right next to the given block
     * @param previousHash specifies the hash of the previous block to use for querying
     * @return Optional wrapper object containing BlockHeader if query returned a BlockHeader, otherwise the wrapper contains null value
     */
    Optional<Block> findBlockHeaderByPreviousHash(String previousHash);

    /**
     * Attempts to find a block which has specified merkle tree root
     * @param merkleTreeRoot specifies the merkle tree root used for querying
     * @return Optional wrapper object containing BlockHeader instance if query returned objects, otherwise the wrapper contains null value
     */
    Optional<Block> findBlockHeaderByMerkleTree(MerkleTreeNode merkleTreeRoot);

    /**
     * Queries all recently mined blocks with specified limit
     * @param limit specifies the amount of blocks to query for
     * @return a list containing BlockHeaders which have recently been mined
     */
    @Query(value = "SELECT * FROM blocks ORDER BY mined_timestamp DESC limit ?1", nativeQuery = true)
    List<Block> findAllByOrderByMinedTimestampLimit(Integer limit);

    /**
     * Counts how many mined blocks exist in the blockchain and returns the total amount
     * @return amount of blocks in blockchain
     */
    @Query(value = "SELECT COUNT(*) FROM BLOCKS WHERE SUBSTR(HASH, 1, 10) = '0000000000'", nativeQuery = true)
    Long findTotalAmountOfMinedBlocks();

    /**
     * Recursively traverses transactions Merkle tree and returns a Block instance whose specified Merkle tree root is the traversed tree's root
     * @param hash specifies the hash of the merkle tree root to use as an entry point for traversal
     * @return Optional wrapper object containing BlockHeader instance if query returned objects, otherwise the wrapper contains a null value
     */
    @Query(value = """
        WITH RECURSIVE SEARCH_PARENT (parent_id, id, hash) AS (
            SELECT i.PARENT_ID, i.ID, i.HASH
            FROM INTERMEDIATE_MERKLE_TREE_NODES i
            WHERE i.HASH = ?1
            UNION ALL
            SELECT i.PARENT_ID, i.ID, i.HASH
            FROM INTERMEDIATE_MERKLE_TREE_NODES i
            JOIN SEARCH_PARENT s ON s.PARENT_ID = i.ID
        )
        SELECT TOP 1 * FROM BLOCKS
        WHERE MERKLE_TREE_ROOT = (SELECT ID FROM SEARCH_PARENT WHERE parent_id IS NULL);
    """, nativeQuery = true)
    Optional<Block> findBlockByMerkleTreeNodeHash(String hash);

    /**
     * Recursively searches for a block with the longest chain and returns a Block instance if such block was found
     * @return a block object representing the Block with the longest chain
     */
    @Query(value = """
        WITH RECURSIVE SEARCH_LONGEST (PREVIOUS_HASH, HASH, CHAIN_LENGTH) AS (
            SELECT b.PREVIOUS_HASH, b.HASH, 1 as CHAIN_LENGTH
            FROM BLOCKS b
            WHERE PREVIOUS_HASH = '0000000000000000000000000000000000000000000000000000000000000000'
            UNION ALL
            SELECT b.PREVIOUS_HASH, b.HASH, s.CHAIN_LENGTH + 1 AS CHAIN_LENGTH
            FROM BLOCKS b
            JOIN SEARCH_LONGEST s ON s.HASH = b.PREVIOUS_HASH
        )
        SELECT TOP 1 * FROM BLOCKS
        WHERE HASH = (SELECT TOP 1 HASH FROM SEARCH_LONGEST ORDER BY CHAIN_LENGTH DESC)
    """, nativeQuery = true)
    Block findBlockWithLongestChain();
}
