package org.students.simplebitcoinnode.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.students.simplebitcoinnode.entity.TransactionOutput;

import java.security.KeyPair;

@Getter
@Setter
@AllArgsConstructor
public class Wallet {
    private KeyPair keyPair;
    private TransactionOutput unspentTransactionOutput;
    private Integer depthLevel;
}
