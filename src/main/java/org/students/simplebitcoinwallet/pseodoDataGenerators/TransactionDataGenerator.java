package org.students.simplebitcoinwallet.pseodoDataGenerators;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedKeyException;
import org.students.simplebitcoinwallet.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinwallet.exceptions.encoding.SerializationException;
import org.students.simplebitcoinwallet.repository.TransactionRepository;
import org.students.simplebitcoinwallet.service.AsymmetricCryptographyService;
import org.students.simplebitcoinwallet.util.Encoding;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class TransactionDataGenerator implements CommandLineRunner {


    private final AsymmetricCryptographyService asymmetricCryptographyService;

    private final TransactionRepository transactionRepository;

    public TransactionDataGenerator(AsymmetricCryptographyService asymmetricCryptographyService, TransactionRepository transactionRepository) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.transactionRepository = transactionRepository;
    }

    private static final Random RANDOM = new Random();

    @Override
    public void run(String... args) throws Exception {
        Transaction transaction = generateTransaction();
        transactionRepository.save(transaction);
    }

    private Transaction generateTransaction() throws InvalidEncodedStringException, MalformedKeyException {
        Transaction transaction = new Transaction();
        transaction.setSenderPublicKey(generateRandomString(177));
        transaction.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
        transaction.setInputs(new ArrayList<>());
        transaction.setOutputs(new ArrayList<>());
        String transactionHash = Encoding.toHexString(asymmetricCryptographyService.digestObject(transaction));
        transaction.setInputs(Arrays.asList(generateTransactionOutput(transactionHash), generateTransactionOutput(transactionHash)));
        transaction.setOutputs(Arrays.asList(generateTransactionOutput(transactionHash), generateTransactionOutput(transactionHash)));
        transaction.setTransactionHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(transaction)));
        return transaction;
    }

    private TransactionOutput generateTransactionOutput(String transaction) throws MalformedKeyException, InvalidEncodedStringException {
        TransactionOutput output = new TransactionOutput();
        KeyPair keys = asymmetricCryptographyService.generateNewKeypair();
        byte[] messagePartFirst = HexFormat.of().parseHex(transaction);
        byte[] messagePartSecond = String.valueOf(keys.getPublic()).getBytes();
        byte[] message = new byte[messagePartFirst.length + messagePartSecond.length];

        System.arraycopy(messagePartFirst,0,message,0,messagePartFirst.length);
        System.arraycopy(messagePartSecond,0,message,messagePartFirst.length,messagePartSecond.length);

        System.out.println(Arrays.toString(message));

        output.setAmount(BigDecimal.valueOf(RANDOM.nextDouble() * 1000));
        output.setReceiverPublicKey(generateRandomString(177));
        output.setSignature(Encoding.toHexString(asymmetricCryptographyService.signMessage(message, keys.getPrivate().getEncoded())));
        return output;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(RANDOM.nextInt(characters.length())));
        }
        return result.toString();
    }
}
