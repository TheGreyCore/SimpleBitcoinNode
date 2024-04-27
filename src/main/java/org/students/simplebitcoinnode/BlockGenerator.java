package org.students.simplebitcoinnode;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.students.simplebitcoinnode.entity.BlockHeader;
import org.students.simplebitcoinnode.entity.MerkleTreeNode;
import org.students.simplebitcoinnode.repository.BlockHeaderRepository;
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
    private final BlockHeaderRepository blockHeaderRepository;
    private final MerkleTreeNodeRepository merkleTreeNodeRepository;
    private final AsymmetricCryptographyService asymmetricCryptographyService;

    // lists for containing generated data
    private final List<MerkleTreeNode> merkleTreeRootNodes = new ArrayList<>();
    private final List<BlockHeader> blockHeaders = new ArrayList<>();

    Logger logger = Logger.getLogger(BlockGenerator.class.getName());

    public BlockGenerator(BlockHeaderRepository blockHeaderRepository, MerkleTreeNodeRepository merkleTreeNodeRepository, AsymmetricCryptographyService asymmetricCryptographyService) {
        this.blockHeaderRepository = blockHeaderRepository;
        this.merkleTreeNodeRepository = merkleTreeNodeRepository;
        this.asymmetricCryptographyService = asymmetricCryptographyService;
    }

    /**
     * Overridden run method for BlockGenerator's CommandLineRunner interface
     * @param args specifies the command line arguments provided for the application
     */
    @Override
    public void run(String... args) throws Exception {
        if (args.length < 3 || !args[0].equals("seed-random-blocks")) {
            System.err.println("Usage: java BlockGenerator <merkle-tree-leaves> <block-count> [mined <N>]");
            System.exit(1);
        }

        long merkleTreeLeaves = ((long)Integer.parseInt(args[1]) & 0xffffffffL) - 1L;
        // round up to the next power of 2
        merkleTreeLeaves |= merkleTreeLeaves >> 1;
        merkleTreeLeaves |= merkleTreeLeaves >> 2;
        merkleTreeLeaves |= merkleTreeLeaves >> 4;
        merkleTreeLeaves |= merkleTreeLeaves >> 8;
        merkleTreeLeaves |= merkleTreeLeaves >> 16;
        merkleTreeLeaves++;

        final int blockCount = Integer.parseInt(args[2]);

        logger.info("Generating " + blockCount + " random blocks...");
        generateBlocks((int)merkleTreeLeaves, blockCount);

        logger.info("Persisting block headers and merkle tree root nodes...");
        blockHeaderRepository.saveAll(blockHeaders);
        merkleTreeNodeRepository.saveAll(merkleTreeRootNodes);
        logger.info("Random block generation is done");
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
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setMerkleTreeRoot(merkleTreeNode.getHash());

            // if previous block exists chain given block to the previous one
            if (!merkleTreeRootNodes.isEmpty())
                blockHeader.setPreviousHash(merkleTreeRootNodes.get(i-1).getHash());
            else blockHeader.setPreviousHash("0".repeat(64));

            // for now, we use 1 as the nonce value
            blockHeader.setNonce(new BigInteger("1"));
            blockHeader.setBlockAssemblyTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
            blockHeader.setMinedTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
            blockHeader.setHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(blockHeader)));

            // append to lists
            blockHeaders.add(blockHeader);
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
