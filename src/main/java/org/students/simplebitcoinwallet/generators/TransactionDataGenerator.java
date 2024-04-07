package org.students.simplebitcoinwallet.generators;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.repository.TransactionRepository;
import org.students.simplebitcoinwallet.service.AsymmetricCryptographyService;
import org.students.simplebitcoinwallet.util.Encoding;
import org.students.simplebitcoinwallet.util.Wallet;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class TransactionDataGenerator implements CommandLineRunner {
    // injected dependencies
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final TransactionRepository transactionRepository;

    // initialized from TransactionDataGenerator.run
    private BigDecimal initialCirculation; // how many tokens should be let into circulation
    private int maxRecipientsFromWallet;   // to how many different recipients should each wallet send tokens
    private int maxTreeDepth;              // how deep should the transaction tree be

    // list of Transactions
    List<Transaction> transactions = new ArrayList<>();

    public TransactionDataGenerator(AsymmetricCryptographyService asymmetricCryptographyService, TransactionRepository transactionRepository) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // check if enough arguments were specified
        if (args.length < 4) {
            return;
        }

        // generation variables
        this.initialCirculation = new BigDecimal(args[1]);
        this.maxRecipientsFromWallet = Integer.parseInt(args[2]);
        this.maxTreeDepth = Integer.parseInt(args[3]);

        generateTransactions();
        transactionRepository.saveAll(transactions);
    }


    private TransactionOutput makeSatoshiUTXO(KeyPair satoshiWallet) {
        return TransactionOutput.builder()
            .signature("0".repeat(142))
            .amount(initialCirculation)
            .receiverPublicKey(Encoding.defaultPubKeyEncoding(satoshiWallet.getPublic().getEncoded()))
            .build();
    }

    private void generateTransactions() throws Exception {
        // generate the initial seed TransactionOutput (aka mint some coins into the circulation)
        KeyPair satoshiWalletKeys = asymmetricCryptographyService.generateNewKeypair();

        // queue is used for breadth-first tree construction
        Queue<Wallet> queue = new ArrayDeque<>();
        queue.add(new Wallet(satoshiWalletKeys, new LinkedList<>(Collections.singletonList(makeSatoshiUTXO(satoshiWalletKeys))), 0));

        // start adding new transactions
        while (!queue.isEmpty()) {
            Wallet wallet = queue.poll();
            if (wallet.getDepthLevel() >= maxTreeDepth)
                break;

            // sum up how much money does the wallet have
            BigDecimal sum = BigDecimal.ZERO;
            for (TransactionOutput output : wallet.getUnspentTransactionOutputs())
                sum = sum.add(output.getAmount());

            for (int i = 0; i < maxRecipientsFromWallet; i++) {
                Random random = new Random(System.nanoTime());
                double f64transferAmount = random.nextDouble(0.00000001, sum.doubleValue() / (double)maxRecipientsFromWallet);
                // transfer amount with 8 digit precision
                final BigDecimal transferAmount = new BigDecimal(String.format("%.8f", f64transferAmount));

                List<TransactionOutput> utxos = wallet.getUnspentTransactionOutputs();
                List<TransactionOutput> bestTransactionInputs = findBestTransactionInputs(utxos, transferAmount);

                // remove bestCombo elements from utxos
                for (TransactionOutput input : bestTransactionInputs)
                    utxos.remove(input);

                // construct a new Transaction object
                KeyPair receiverKeys = asymmetricCryptographyService.generateNewKeypair();
                Transaction transaction = createNewTransaction(bestTransactionInputs, wallet.getKeyPair(), receiverKeys, transferAmount);

                // check if there is change TransactionOutput and add it to UTXOs
                if (transaction.getOutputs().size() == 2)
                    utxos.add(transaction.getOutputs().get(1));

                // create a new Wallet instance and add it to the queue
                Wallet newWallet = new Wallet(receiverKeys, transaction.getOutputs(), wallet.getDepthLevel() + 1);
                queue.add(newWallet);

                // add Transaction to the list
                transactions.add(transaction);
            }
        }
    }

    private Transaction createNewTransaction(List<TransactionOutput> inputs, KeyPair senderKeys, KeyPair receiverKeys, BigDecimal sum) throws Exception {
        // calculate the amount of change that needs to be returned to the sender
        BigDecimal change = BigDecimal.ZERO;
        for (TransactionOutput input : inputs)
            change = change.add(input.getAmount());
        change = change.subtract(sum);

        // construct the transaction
        Transaction transaction = new Transaction();
        transaction.setInputs(inputs);
        transaction.setOutputs(new LinkedList<>());
        transaction.setSenderPublicKey(Encoding.defaultPubKeyEncoding(senderKeys.getPublic().getEncoded()));
        transaction.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
        transaction.getOutputs().add(
            TransactionOutput.builder()
                .amount(sum)
                .receiverPublicKey(Encoding.defaultPubKeyEncoding(receiverKeys.getPublic().getEncoded()))
                .build()
        );

        // check if change needs to be returned to the sender
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            transaction.getOutputs().add(
                TransactionOutput.builder()
                    .amount(change)
                    .receiverPublicKey(Encoding.defaultPubKeyEncoding(senderKeys.getPublic().getEncoded()))
                    .build()
            );
        }

        // calculate transaction hash
        transaction.setTransactionHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(transaction)));

        // sign every transaction output
        for (TransactionOutput output : transaction.getOutputs()) {
            // create a message to sign
            final byte[] hash = Encoding.hexStringToBytes(transaction.getTransactionHash());
            final byte[] receiverPublicKey = Encoding.defaultPubKeyDecoding(output.getReceiverPublicKey());
            final byte[] signatureMessage = Arrays.copyOf(hash, hash.length + receiverPublicKey.length);
            System.arraycopy(receiverPublicKey, 0, signatureMessage, hash.length, receiverPublicKey.length);

            // calculate TransactionOutput signature and set it
            output.setSignature(Encoding.toHexString(asymmetricCryptographyService.signMessage(signatureMessage, senderKeys.getPrivate().getEncoded())));
        }

        return transaction;
    }


    private List<TransactionOutput> findBestTransactionInputs(List<TransactionOutput> utxos, BigDecimal requiredSum) {
        utxos.sort(Comparator.comparing(TransactionOutput::getAmount));
        List<TransactionOutput> inputs = new LinkedList<>();

        BigDecimal sum = BigDecimal.ZERO;
        for (TransactionOutput utxo : utxos) {
            sum = sum.add(utxo.getAmount());
            inputs.add(utxo);

            if (sum.compareTo(requiredSum) >= 0)
                break;
        }

        return inputs;
    }
}
