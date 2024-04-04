package org.students.simplebitcoinwallet.unit.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.util.Encoding;

@NoArgsConstructor
@AllArgsConstructor
public class TransactionContainer {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Transaction transaction;

    @Getter
    @Setter
    private byte[] hash;

    @Override
    public String toString() {
        return name + ": " + Encoding.toHexString(hash);
    }
}
