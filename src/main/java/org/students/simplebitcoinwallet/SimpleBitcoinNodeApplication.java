package org.students.simplebitcoinwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class SimpleBitcoinNodeApplication {
    public static void main(String[] args) {
        // insert bouncycastle provider as default Security provider
        Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);
        SpringApplication.run(SimpleBitcoinNodeApplication.class, args);
    }
}
