package org.students.simplebitcoinnode.event.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.students.simplebitcoinnode.entity.Block;
import org.students.simplebitcoinnode.event.BlockMinedEvent;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.util.Encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Worker class that mines a specified region of the block nonce in one thread
 */
public class MineWorker implements Runnable {
    private final Logger logger = Logger.getLogger(MineWorker.class.getName());

    // injected dependencies
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final ApplicationEventPublisher applicationEventPublisher;

    // mining parameters
    private final BigInteger offset;
    private final BigInteger stride;
    private final Long zeroBitCondition;
    private final Block block;
    private final AtomicBoolean continueMining;

    public MineWorker(AsymmetricCryptographyService asymmetricCryptographyService,
                      ApplicationEventPublisher applicationEventPublisher,
                      BigInteger offset,
                      BigInteger stride,
                      Long zeroBitCondition,
                      Block block,
                      AtomicBoolean continueMining) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.offset = offset;
        this.stride = stride;
        this.zeroBitCondition = zeroBitCondition;
        this.block = block;
        this.continueMining = continueMining;
    }

    /**
     * Main function that starts block mining with specified offset and stride
     */
    @Override
    public void run() {
        block.setNonce(offset);
        // for resource optimization reasons, we use digestBytes instead of digestObject using stream objects that have been constructed only once
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            // prefixVal is the first 64 bits of the SHA256 hash of the block
            //  this value is used to check if zeroBitCondition is met
            long prefixVal = castPrefixValBigEndian(Encoding.hexStringToBytes(block.getHash()));

            while (continueMining.get() && (prefixVal & ((1L << zeroBitCondition) - 1)) != 0) {
                objectOutputStream.reset();
                byteArrayOutputStream.reset();
                block.writeExternal(objectOutputStream);
                objectOutputStream.flush();
                byte[] bytes = byteArrayOutputStream.toByteArray();
                byte[] hash = asymmetricCryptographyService.digestBytes(bytes);
                block.setHash(Encoding.toHexString(hash));
                prefixVal = castPrefixValBigEndian(hash);
                if ((prefixVal & ((1L << zeroBitCondition) - 1)) != 0)
                    block.setNonce(block.getNonce().add(stride));
            }

            // block was mined on this thread
            if ((prefixVal & ((1L << zeroBitCondition) - 1)) == 0) {
                continueMining.set(false);
                block.setMinedTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
                applicationEventPublisher.publishEvent(new BlockMinedEvent(this, block));
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
}
