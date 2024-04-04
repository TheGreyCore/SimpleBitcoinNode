package org.students.simplebitcoinwallet.unit.transaction;

import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Base class for providing transaction
 */
public abstract class TransactionHasher {
    /**
     * Serializes transaction according to provided defined Transaction serialization standard.
     * @param transaction specifies the transaction object to serialize
     * @return byte array containing the byte serialized Transaction object
     */
    protected byte[] byteSerializeTransaction(TransactionContainer transaction) throws Exception {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream))
        {
            // serialize the number of input transactions
            out.writeObject(transaction.getTransaction().getInputs().size());
            // serialize transaction inputs according to specification
            for (TransactionOutput output : transaction.getTransaction().getInputs()) {
                out.writeObject(output.getSignature());
                out.writeObject(output.getAmount());
                out.writeObject(output.getReceiverPublicKey());
            }
            // serialize the number of output transactions
            out.writeObject(transaction.getTransaction().getOutputs().size());
            // serialize transaction outputs according to specification
            for (TransactionOutput output : transaction.getTransaction().getOutputs()) {
                out.writeObject(output.getAmount());
                out.writeObject(output.getReceiverPublicKey());
            }
            // serialize sender public key
            out.writeObject(transaction.getTransaction().getSenderPublicKey());
            // serialize timestamp
            out.writeObject(transaction.getTransaction().getTimestamp());
            out.flush();

            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Hashes Transaction object according to implementation specific hashing algorithm
     * @param transaction specifies the Transaction object to use for hashing
     * @return byte array containing the hash of the serialized Transaction object
     */
    public abstract void hashTransaction(TransactionContainer transaction) throws Exception;
}
