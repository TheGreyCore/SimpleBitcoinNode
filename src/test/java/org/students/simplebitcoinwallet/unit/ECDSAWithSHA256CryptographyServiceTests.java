package org.students.simplebitcoinwallet.unit;

import org.junit.jupiter.api.BeforeAll;
import org.students.simplebitcoinwallet.service.impl.ECDSAWithSHA256CryptographicService;
import java.security.Security;


public class ECDSAWithSHA256CryptographyServiceTests extends AsymmetricCryptographyServiceSHA256Tests {
    @BeforeAll
    public static void setUpBeforeClass() {
        Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);
        asymmetricCryptographyService = new ECDSAWithSHA256CryptographicService();
    }
}
