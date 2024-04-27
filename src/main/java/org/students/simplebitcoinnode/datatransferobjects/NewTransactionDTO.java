package org.students.simplebitcoinnode.datatransferobjects;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NotNull
@AllArgsConstructor
@NoArgsConstructor
public class NewTransactionDTO {
    private String transactionHash;
    private List<TransactionOutputDTO> inputs;
    private List<TransactionOutputDTO> outputs;
    private String senderPublicKey;
    private LocalDateTime timestamp;
}
