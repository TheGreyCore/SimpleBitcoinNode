package org.students.simplebitcoinnode;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;

@SpringBootApplication
public class BlockGenerator implements CommandLineRunner {
    private final AsymmetricCryptographyService asymmetricCryptographyService;

    public BlockGenerator(AsymmetricCryptographyService asymmetricCryptographyService) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java -jar .jar <block hash>");
        }
    }
}
