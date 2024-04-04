package org.students.simplebitcoinwallet.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.students.simplebitcoinwallet.exceptions.encoding.SerializationException;
import org.students.simplebitcoinwallet.unit.transaction.SHA256TransactionHasher;
import org.students.simplebitcoinwallet.unit.transaction.TestTransactionBuilder;
import org.students.simplebitcoinwallet.unit.transaction.TransactionContainer;
import org.students.simplebitcoinwallet.unit.transaction.TransactionHasher;
import org.students.simplebitcoinwallet.util.Encoding;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AsymmetricCryptographyServiceSHA256Tests extends AsymmetricCryptographyServiceTests {
    @Test
    @DisplayName("Ensure that SHA-256 digest produces correct result for non-Transaction types")
    public void testDigest_EnsureMatchingSHA256Digest_SerializableString() {
        final String[] msgs = {"Hello world!", "hello world!", "Yoooooo!", "Hash me pls"};
        final String[] hashes = {
                "c8af75eb8aea98fc3dd2737404a7ed9e3a57c7f597ad56af9f342a1e66c48212",
                "8c5798e47b0160f79040b678184e40bb57419d5d954a41bee48975b0e0e4e520",
                "1de205d4dabd597a8464e4199bc948a29585532ee090477598815100d4dc2a80",
                "b9e5c80e5cfa91c4023416708c752425b0866cfd3a238293e3276db69c7a74eb"
        };

        try {
            for (int i = 0; i < msgs.length; i++) {
                assertEquals(hashes[i], Encoding.toHexString(asymmetricCryptographyService.digestObject(msgs[i])));
            }
        } catch (SerializationException e) {
            fail();
        }
    }

    @Test
    @DisplayName("Ensure that SHA-256 digest produces correct result for Transaction types")
    public void testDigest_EnsureMatchingSHA256Digest_SerializableTransaction() throws Exception {
        TransactionHasher hasher = new SHA256TransactionHasher();

        List<TransactionContainer> testTransactions = TestTransactionBuilder.buildAllTransactions();
        // calculate expected hashes for testTransactions
        for (TransactionContainer container : testTransactions) {
            hasher.hashTransaction(container);
        }

        // check if asymmetricCryptographyService gives same hashes
        for (TransactionContainer container : testTransactions) {
            byte[] calculatedHash = asymmetricCryptographyService.digestObject(container.getTransaction());
            assertEquals(Encoding.toHexString(container.getHash()), Encoding.toHexString(calculatedHash));
        }
    }
}
