package org.students.simplebitcoinwallet.dataTransferObjects;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.students.simplebitcoinwallet.entity.TransactionOutput;

import java.util.List;

@Setter
@Getter
@NotNull
@AllArgsConstructor
public class NewTransactionDTO {
    private String transactionHash;
    private List<TransactionOutput> inputs;
    private List<TransactionOutput> outputs;
    private String senderPublicKey;
}
