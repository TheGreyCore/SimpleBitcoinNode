package org.students.simplebitcoinnode.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.service.BlockBuilderService;
import org.students.simplebitcoinnode.service.impl.BlockBuilderServiceImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class BlockBuilderServiceTests {
    @Mock
    public AsymmetricCryptographyService asymmetricCryptographyService;

    @Test
    @DisplayName("Ensure that all given transactions exist in block's merkle tree")
    public void testMerkleTreeTransactions_EnsureThatAllGivenTransactionsExistInBlock() throws Exception {
        Set<Transaction> transactions = new HashSet<>(Set.of(
                Transaction.builder().transactionHash("4355a46b19d348dc2f57c046f8ef63d4538ebb936000f3c9ee954a27460dd865").build(),
                Transaction.builder().transactionHash("01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b").build(),
                Transaction.builder().transactionHash("53c234e5e8472b6ac51c1ae1cab3fe06fad053beb8ebfd8977b010655bfdd3c3").build(),
                Transaction.builder().transactionHash("1121cfccd5913f0a63fec40a6ffd44ea64f9dc135c66634ba001d10bcf4302a2").build(),
                Transaction.builder().transactionHash("7de1555df0c2700329e815b93b32c571c3ea54dc967b89e81ab73b9972b72d1d").build()));

        given(asymmetricCryptographyService.digestObject(any()))
                .willReturn(new byte[32]);

        BlockBuilderService blockBuilderService = new BlockBuilderServiceImpl(asymmetricCryptographyService);
        MerkleTreeNode root = blockBuilderService.createMerkleTreeRoot(transactions);

        Queue<MerkleTreeNode> nodeQueue = new ArrayDeque<>();
        nodeQueue.add(root);
        while (!nodeQueue.isEmpty()) {
            MerkleTreeNode node = nodeQueue.poll();
            if (node.getChildren() != null)
                nodeQueue.addAll(node.getChildren());
            if (node.getTransaction() != null) {
                assertTrue(transactions.contains(node.getTransaction()));
                transactions.remove(node.getTransaction());
            }
        }

        assertEquals(0, transactions.size());
    }

    @Test
    @DisplayName("Ensure that the built block contains required variables")
    public void testNewBlock_EnsureThatBlockContainsRequiredVariables() throws Exception {
        MerkleTreeNode root = MerkleTreeNode.builder().hash("0".repeat(64)).build();

        final byte[] blockHash = new byte[32];
        Arrays.fill(blockHash, (byte)0x22);
        final String strBlockHash = "2".repeat(64);
        final String previousBlockHash = "1".repeat(64);

        given(asymmetricCryptographyService.digestObject(any()))
                .willReturn(blockHash);

        BlockBuilderService blockBuilderService = new BlockBuilderServiceImpl(asymmetricCryptographyService);
        Block block = blockBuilderService.newBlock(root, previousBlockHash);

        assertEquals(strBlockHash, block.getHash());
        assertEquals(root, block.getMerkleTree());
        assertEquals(previousBlockHash, block.getPreviousHash());
        assertEquals(BigInteger.ZERO, block.getNonce());
        assertNotNull(block.getBlockAssemblyTimestamp());
        assertNotNull(block.getMiners());
        assertNull(block.getId());
        assertNull(block.getMinedTimestamp());
        assertNotNull(block.getBlockAssemblyTimestamp());
    }

    @Test
    @DisplayName("Ensure that coinbase transaction gets constructed properly and rewards distributed evenly")
    public void testMakeCoinbaseTransaction_EnsureThatCoinbaseTransactionGetsConstructedProperly() throws Exception {
        final BigDecimal blockReward = BigDecimal.valueOf(50);
        Set<String> minerWalletAddresses = new HashSet<>(Set.of(
            "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCxLicYMb1uMm11hdcppoo28PRnsLfuegR8yz9cakjXM1B3HhNK9FykvRfCu3uS3urm8WyCmmHsquFVWmDMMNxptcV",
            "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCx6xpuUnRfaCBFA2QQrCwKwGZpFywTtJc76oPPwaQ8SrtFK3zW3usBpvhtK9khk2XBArWZ2NStMHeXXpibKSJisnf",
            "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCzQjPG1LdPyVZ6J5Gs6GFwGbLMciVLCAg51kPuk1HrNJL6FozwoJuU2REDNP5ZQLPaA4zqAadUp5MYPhiQ3brSUbG",
            "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCxJZoDfW2gJemgZMuUY6hH29EtoKHKwqKMqxCnDwkzn6TXUQXXr7AKm7io7DvTL1w7AHsmgSqTL9phCgM68GpFC1J",
            "PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCwHrqq9aktPqigAAT2yc61xGsS6FB4bUjqVXJnSZuSX1fJmnw16Q6kxzzNeRQrx1GJsrCtxQc7TEk8xY6tXpFiEUm"));

        final BigDecimal rewardPerWallet = blockReward.divide(BigDecimal.valueOf(minerWalletAddresses.size()), 8, RoundingMode.HALF_DOWN);
        final byte[] transactionHash = new byte[32];
        final String strTransactionHash = "1".repeat(64);
        Arrays.fill(transactionHash, (byte)0x11);

        given(asymmetricCryptographyService.digestObject(any()))
                .willReturn(transactionHash);

        BlockBuilderService blockBuilderService = new BlockBuilderServiceImpl(asymmetricCryptographyService);
        Transaction transaction = blockBuilderService.makeCoinbaseTransaction(minerWalletAddresses, blockReward);

        assertNull(transaction.getId());
        assertNull(transaction.getInputs());
        assertNotNull(transaction.getTimestamp());
        assertEquals(strTransactionHash, transaction.getTransactionHash());
        transaction.getOutputs().forEach((output) -> {
            assertEquals("0".repeat(144), output.getSignature());
            assertTrue(minerWalletAddresses.contains(output.getReceiverPublicKey()));
            minerWalletAddresses.remove(output.getReceiverPublicKey());
            assertEquals(rewardPerWallet, output.getAmount());
            assertNull(output.getId());
        });
    }
}
