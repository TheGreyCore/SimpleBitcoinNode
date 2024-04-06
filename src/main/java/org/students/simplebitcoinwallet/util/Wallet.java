package org.students.simplebitcoinwallet.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.students.simplebitcoinwallet.entity.TransactionOutput;

import java.security.KeyPair;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Wallet {
    private KeyPair keyPair;
    private List<TransactionOutput> unspentTransactionOutputs;
    private Integer depthLevel;
}
