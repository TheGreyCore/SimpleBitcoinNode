package org.students.simplebitcoinnode.unit.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.util.Encoding;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionContainer {
    private String name;
    private TransactionDTO transaction;
    private byte[] hash;

    @Override
    public String toString() {
        return name + ": " + Encoding.toHexString(hash);
    }
}
