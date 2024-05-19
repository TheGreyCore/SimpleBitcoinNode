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

public class MineWorker implements Runnable {
    private final Logger logger = Logger.getLogger(MineWorker.class.getName());
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final ApplicationEventPublisher applicationEventPublisher;
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

    @Override
    public void run() {
        block.setNonce(offset);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
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
