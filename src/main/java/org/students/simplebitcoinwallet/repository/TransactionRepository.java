package org.students.simplebitcoinwallet.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.students.simplebitcoinwallet.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

}
