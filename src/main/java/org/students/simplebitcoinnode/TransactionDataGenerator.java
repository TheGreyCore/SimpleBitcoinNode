package org.students.simplebitcoinnode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.entity.TransactionOutput;
import org.students.simplebitcoinnode.repository.TransactionRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.util.Encoding;
import org.students.simplebitcoinnode.util.Wallet;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Logger;
import java.util.*;

@SpringBootApplication
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

    @Autowired
    public TransactionDataGenerator(AsymmetricCryptographyService asymmetricCryptographyService, TransactionRepository transactionRepository) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Logger logger = Logger.getLogger(TransactionDataGenerator.class.getName());

        if (args.length < 4 || !args[0].equals("seed-transactions"))
            return;

        logger.info("Running pseudo-data generating");
        // generation variables
        logger.info("Starting transaction data generation");
        this.initialCirculation = new BigDecimal(args[1]);
        this.maxRecipientsFromWallet = Integer.parseInt(args[2]);
        this.maxTreeDepth = Integer.parseInt(args[3]);

        generateTransactions();
        logger.info("Finished transaction data generation");
        logger.info("Saving transaction data into TransactionRepository");
        transactionRepository.saveAll(transactions);
    }

    /**
     * Create the very first transaction output (aka mint tokens into circulation)
     * @param satoshiWallet specifies a keypair of the very first wallet holding all tokens in circulation
     * @return
     */
    private TransactionOutput makeSatoshiUTXO(KeyPair satoshiWallet) {
        return TransactionOutput.builder()
            .signature("0".repeat(142))
            .amount(initialCirculation)
            .receiverPublicKey(Encoding.defaultPubKeyEncoding(satoshiWallet.getPublic().getEncoded()))
            .build();
    }

    /**
     * Generate random transactions according to specified parameters.<br>
     * The transaction generation algorithm works by constructing a tree from generated transactions.
     * Initially, Satoshi holds some amount of tokens, of which most of it he sends to N other wallets and those N wallets send to other N wallets and so on.
     * @throws Exception
     */
    private void generateTransactions() throws Exception {
        // generate the initial seed TransactionOutput (aka mint some coins into the circulation)
        KeyPair satoshiWalletKeys = asymmetricCryptographyService.generateNewKeypair();

        // queue is used for breadth-first tree construction
        Queue<Wallet> queue = new ArrayDeque<>();
        queue.add(new Wallet(satoshiWalletKeys, makeSatoshiUTXO(satoshiWalletKeys), 0));

        // start adding new transactions
        while (!queue.isEmpty()) {
            Wallet wallet = queue.poll();
            if (wallet.getDepthLevel() >= maxTreeDepth)
                break;

            // sum up how much money does the wallet have
            final BigDecimal sum = wallet.getUnspentTransactionOutput().getAmount();

            for (int i = 0; i < maxRecipientsFromWallet; i++) {
                Random random = new Random(System.nanoTime());
                double f64transferAmount = random.nextDouble(0.00000001, sum.doubleValue() / (double)maxRecipientsFromWallet);
                // transfer amount with 8 digit precision
                final BigDecimal transferAmount = new BigDecimal(String.format("%.8f", f64transferAmount));

                // construct a new Transaction object
                KeyPair receiverKeys = asymmetricCryptographyService.generateNewKeypair();
                Transaction transaction = createNewTransaction(Collections.singletonList(wallet.getUnspentTransactionOutput()), wallet.getKeyPair(), receiverKeys, transferAmount);

                // set returned change as new UTXO of current wallet
                if (transaction.getOutputs().size() >= 2)
                    wallet.setUnspentTransactionOutput(transaction.getOutputs().get(1));

                // create a new Wallet instance and add it to the queue
                Wallet newWallet = new Wallet(receiverKeys, transaction.getOutputs().getFirst(), wallet.getDepthLevel() + 1);
                queue.add(newWallet);

                // add Transaction to the list
                transactions.add(transaction);
            }
        }
    }

    /**
     * Builds a new transaction from given data
     * @param inputs specifies unspent transaction outputs to use as inputs for new transaction
     * @param senderKeys specifies the sender's wallet keypair
     * @param receiverKeys receiver's wallet keypair
     * @param sum total sum of tokens to send to receiver's address
     * @return transaction object specifies the transaction constructed from given arguments
     * @throws Exception
     */
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
        transaction.getOutputs().add(TransactionOutput.builder()
            .amount(sum)
            .receiverPublicKey(Encoding.defaultPubKeyEncoding(receiverKeys.getPublic().getEncoded()))
            .build());

        // check if change needs to be returned to the sender
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            transaction.getOutputs().add(TransactionOutput.builder()
                .amount(change)
                .receiverPublicKey(Encoding.defaultPubKeyEncoding(senderKeys.getPublic().getEncoded()))
                .build());
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

    /* Main method */
    public static void main(String[] args) {
        Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);
        SpringApplication.run(TransactionDataGenerator.class, args);
    }
}
