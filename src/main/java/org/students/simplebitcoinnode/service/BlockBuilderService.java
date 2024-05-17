package org.students.simplebitcoinnode.service;

import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.exceptions.encoding.SerializationException;

import java.math.BigDecimal;
import java.util.Collection;

public interface BlockBuilderService {
    /**
     * Calculate merkle tree root SHA256 hash of given transactions
     * @param transactions specifies collection list of transactions to use for hashing
     * @return MerkleTreeNode object representing the merkle tree root
     */
    MerkleTreeNode calculateMerkleTreeRoot(Collection<Transaction> transactions) throws InvalidEncodedStringException;

    /**
     * Create a new block from given transactions
     * @param merkleTreeNode specifies Merkle tree root node to use in the new block
     * @return Block object representing the built block
     */
    Block newBlock(MerkleTreeNode merkleTreeNode, String previousBlockHash);

    /**
     * Build a coinbase transaction from a list of recipient addresses, who shall receive the mining rewards
     * @param recipientWalletAddresses specifies the list of recipients who will receive the mining rewards
     * @param blockReward specifies the amount of tokens, block miner receives after mining the block where given coinbase transaction belongs to
     * @return Transaction object representing the coinbase transaction
     */
    Transaction makeCoinbaseTransaction(Collection<String> recipientWalletAddresses, BigDecimal blockReward) throws SerializationException;
}
