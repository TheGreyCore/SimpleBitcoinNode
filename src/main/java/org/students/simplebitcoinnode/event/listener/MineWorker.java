package org.students.simplebitcoinnode.event.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.event.BlockMinedEvent;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.repository.BlockRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.util.Encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class MineWorker implements Runnable, ApplicationListener<BlockMinedEvent> {
    private final Logger logger = Logger.getLogger(MineWorker.class.getName());
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BlockRepository blockRepository;
    private final BigInteger offset;
    private final BigInteger stride;
    private final Long zeroBitCondition;
    private final Block block;
    private final AtomicBoolean continueMining = new AtomicBoolean(true);

    public MineWorker(AsymmetricCryptographyService asymmetricCryptographyService,
                      ApplicationEventPublisher applicationEventPublisher,
                      BlockRepository blockRepository,
                      BigInteger offset,
                      BigInteger stride,
                      Long zeroBitCondition,
                      Block block) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.blockRepository = blockRepository;
        this.offset = offset;
        this.stride = stride;
        this.zeroBitCondition = zeroBitCondition;
        this.block = block;
    }

    @Override
    public void run() {
        block.setNonce(offset);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            long prefixVal = castPrefixValBigEndian(Encoding.hexStringToBytes(block.getHash()));

            while (continueMining.get() && (prefixVal & ((1L << (zeroBitCondition + 1)) - 1)) == 0) {
                outputStream.reset();
                block.writeExternal(objectOutputStream);
                outputStream.flush();
                byte[] bytes = outputStream.toByteArray();
                byte[] hash = asymmetricCryptographyService.digestBytes(bytes);
                block.setNonce(block.getNonce().add(stride));
                block.setHash(Encoding.toHexString(hash));
            }

            // block was mined on this thread
            if ((prefixVal & ((1L << (zeroBitCondition + 1)) - 1)) == 0) {
                blockRepository.save(block);
                applicationEventPublisher.publishEvent(new BlockMinedEvent(applicationEventPublisher, block.getId()));
            }

        }
        catch (InvalidEncodedStringException e) {
            logger.severe("Invalid initial block hash: " + e.getMessage());
        }
        catch (IOException e) {
            logger.severe("Failed to serialize Block object into byte array: " + e.getMessage());
        }
    }

    private long castPrefixValBigEndian(byte[] hash) {
        return ((hash[7] & 0xFFL) << 56) |
                ((hash[6] & 0xFFL) << 48) |
                ((hash[5] & 0xFFL) << 40) |
                ((hash[4] & 0xFFL) << 32) |
                ((hash[3] & 0xFFL) << 24) |
                ((hash[2] & 0xFFL) << 16) |
                ((hash[1] & 0xFFL) << 8) |
                ((hash[0] & 0xFFL));
    }

    @Override
    public void onApplicationEvent(BlockMinedEvent event) {
        if (event.getId().equals(block.getId()))
            this.continueMining.set(false);
    }
}
