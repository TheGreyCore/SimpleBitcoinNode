package org.students.simplebitcoinnode.service.impl;

import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.entity.TransactionOutput;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.exceptions.encoding.SerializationException;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.service.BlockBuilderService;
import org.students.simplebitcoinnode.util.Encoding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class BlockBuilderServiceImpl implements BlockBuilderService {
    private final AsymmetricCryptographyService asymmetricCryptographyService;

    public BlockBuilderServiceImpl(AsymmetricCryptographyService asymmetricCryptographyService) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
    }

    @Override
    public MerkleTreeNode createMerkleTreeRoot(Collection<Transaction> transactions) throws InvalidEncodedStringException {
        Queue<MerkleTreeNode> treeQueue = new ArrayDeque<>();
        for (Transaction transaction : transactions)
            treeQueue.add(MerkleTreeNode.builder().hash(transaction.getTransactionHash()).transaction(transaction).build());

        while (treeQueue.size() > 1) {
            MerkleTreeNode leftChild = treeQueue.poll();
            MerkleTreeNode rightChild = treeQueue.poll();

            MerkleTreeNode parent = new MerkleTreeNode();
            parent.setChildren(List.of(leftChild, rightChild));

            final byte[] leftChildHash = Encoding.hexStringToBytes(leftChild.getHash());
            final byte[] rightChildHash = Encoding.hexStringToBytes(rightChild.getHash());

            byte[] combinedHash = new byte[leftChildHash.length + rightChildHash.length];
            System.arraycopy(leftChildHash, 0, combinedHash, 0, leftChildHash.length);
            System.arraycopy(rightChildHash, 0, combinedHash, leftChildHash.length, rightChildHash.length);

            final byte[] newHash = asymmetricCryptographyService.digestObject(combinedHash);
            parent.setHash(Encoding.toHexString(newHash));
            treeQueue.add(parent);
        }

        return treeQueue.poll();
    }

    @Override
    public Block newBlock(MerkleTreeNode root, String previousBlockHash) throws SerializationException {
        Block block = new Block();
        block.setPreviousHash(previousBlockHash);
        block.setNonce(BigInteger.ZERO);
        block.setMerkleTree(root);
        block.setMiners(new ArrayList<>());
        block.setHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(block)));
        return block;
    }

    @Override
    public Transaction makeCoinbaseTransaction(Collection<String> recipientWalletAddresses, BigDecimal blockReward) throws SerializationException {
        Transaction transaction = new Transaction();
        transaction.setSenderPublicKey("0".repeat(178));

        List<TransactionOutput> outputs = new ArrayList<>(recipientWalletAddresses.size());
        BigDecimal rewardsPerWallet = blockReward.divide(new BigDecimal(recipientWalletAddresses.size()), 8, RoundingMode.HALF_DOWN);

        for (String recipientWalletAddress : recipientWalletAddresses) {
            outputs.add(TransactionOutput.builder()
                    .amount(rewardsPerWallet)
                    .receiverPublicKey(recipientWalletAddress)
                    .signature("0".repeat(144))
                    .build());
        }

        transaction.setOutputs(outputs);
        transaction.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        transaction.setTransactionHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(transaction)));
        return transaction;
    }
}
