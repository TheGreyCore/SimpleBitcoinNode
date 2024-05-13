package org.students.simplebitcoinnode;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.repository.MerkleTreeNodeRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.util.Encoding;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

@Profile("BlockGenerator")
@SpringBootApplication
public class BlockGenerator implements CommandLineRunner {
    // injected dependencies
    private final BlockRepository blockRepository;
    private final MerkleTreeNodeRepository merkleTreeNodeRepository;
    private final AsymmetricCryptographyService asymmetricCryptographyService;

    // lists for containing generated data
    private final List<MerkleTreeNode> merkleTreeRootNodes = new ArrayList<>();
    private final List<Block> blocks = new ArrayList<>();

    Logger logger = Logger.getLogger(BlockGenerator.class.getName());

    public BlockGenerator(BlockRepository blockRepository, MerkleTreeNodeRepository merkleTreeNodeRepository, AsymmetricCryptographyService asymmetricCryptographyService) {
        this.blockRepository = blockRepository;
        this.merkleTreeNodeRepository = merkleTreeNodeRepository;
        this.asymmetricCryptographyService = asymmetricCryptographyService;
    }

    /**
     * Overridden run method for BlockGenerator's CommandLineRunner interface
     * @param args specifies the command line arguments provided for the application
     */
    @Override
    public void run(String... args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: simple-bitcoin-wallet -Dspring.profiles.active=BlockGenerator <block-count> [mined <N>]");
            System.exit(1);
        }

        long merkleTreeLeaves = ((long)Integer.parseInt(args[0]) & 0xffffffffL) - 1L;
        // round up to the next power of 2
        merkleTreeLeaves |= merkleTreeLeaves >> 1;
        merkleTreeLeaves |= merkleTreeLeaves >> 2;
        merkleTreeLeaves |= merkleTreeLeaves >> 4;
        merkleTreeLeaves |= merkleTreeLeaves >> 8;
        merkleTreeLeaves |= merkleTreeLeaves >> 16;
        merkleTreeLeaves++;

        final int blockCount = Integer.parseInt(args[1]);

        logger.info("Generating " + blockCount + " random blocks...");
        generateBlocks((int)merkleTreeLeaves, blockCount);

        logger.info("Persisting block headers and merkle tree root nodes...");
        blockRepository.saveAll(blocks);
        merkleTreeNodeRepository.saveAll(merkleTreeRootNodes);
        logger.info("Random block generation is done");

        System.exit(0);
    }

    /**
     * Generates random blocks and merkle trees associated with them
     * @param merkleTreeLeaves specifies the number of leaves each merkle tree has
     * @param blockCount specifies the number of blocks to generate
     */
    private void generateBlocks(int merkleTreeLeaves, int blockCount) throws Exception {
        for (int i = 0; i < blockCount; i++) {
            // generate a new Merkle tree
            MerkleTreeNode merkleTreeNode = buildMerkleTree(merkleTreeLeaves * i, merkleTreeLeaves);
            Block block = new Block();
            block.setMerkleTree(merkleTreeNode);

            // if previous block exists chain given block to the previous one
            if (!blocks.isEmpty())
                block.setPreviousHash(blocks.get(i-1).getHash());
            else block.setPreviousHash("0".repeat(64));

            // for now, we use 1 as the nonce value
            block.setNonce(new BigInteger("1"));
            block.setBlockAssemblyTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
            block.setMinedTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
            block.setHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(block)));

            // append to lists
            blocks.add(block);
            merkleTreeRootNodes.add(merkleTreeNode);
        }
    }

    /**
     * Generates a new merkle tree whose leaves are hashed integers
     * @param start specifies the auto incremented leaf index start
     * @param merkleTreeLeaves specifies the amount of leaves to generated
     * @return MerkleTreeNode object representing the root node
     */
    private MerkleTreeNode buildMerkleTree(int start, int merkleTreeLeaves) throws Exception {
        Queue<MerkleTreeNode> treeQueue = new ArrayDeque<>();

        // add tree leaves to the queue
        for (int i = start; i < start + merkleTreeLeaves; i++) {
            MerkleTreeNode leaf = MerkleTreeNode.builder().hash(Encoding.toHexString(asymmetricCryptographyService.digestObject(new BigInteger(Integer.toString(i))))).build();
            treeQueue.add(leaf);
        }

        // start building the Merkle tree
        while (treeQueue.size() > 1) {
            MerkleTreeNode leftChild = treeQueue.poll();
            MerkleTreeNode rightChild = treeQueue.poll();

            MerkleTreeNode parent = new MerkleTreeNode();
            parent.setChildren(List.of(leftChild, rightChild));

            // calculate parent hash from combined child node hashes
            byte[] combinedHashes = new byte[64];
            System.arraycopy(Encoding.hexStringToBytes(leftChild.getHash()), 0, combinedHashes, 0, 32);
            System.arraycopy(Encoding.hexStringToBytes(rightChild.getHash()), 0, combinedHashes, 32, 32);
            parent.setHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(combinedHashes)));
            treeQueue.add(parent);
        }

        return treeQueue.poll();
    }

    public static void main(String[] args) {
        SpringApplication.run(BlockGenerator.class, args);
    }
}
