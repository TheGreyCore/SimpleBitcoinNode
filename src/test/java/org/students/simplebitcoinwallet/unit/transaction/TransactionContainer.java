package org.students.simplebitcoinwallet.unit.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.util.Encoding;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionContainer {
    private String name;
    private Transaction transaction;
    private byte[] hash;

    @Override
    public String toString() {
        return name + ": " + Encoding.toHexString(hash);
    }
}
