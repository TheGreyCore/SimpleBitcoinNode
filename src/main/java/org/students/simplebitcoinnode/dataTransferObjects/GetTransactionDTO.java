package org.students.simplebitcoinnode.dataTransferObjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.students.simplebitcoinnode.entity.TransactionOutput;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GetTransactionDTO {
    private String transactionHash;
    private List<TransactionOutputDTO> inputs;
    private List<TransactionOutputDTO> outputs;
    private String senderPublicKey;
    private LocalDateTime timestamp;
}
