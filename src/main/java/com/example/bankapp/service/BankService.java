package com.example.bankapp.service;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.BankException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditService auditService;

    // Transfer money between two customers
    @Transactional(rollbackFor = Exception.class)
   //@Transactional(propagation = Propagation.SUPPORTS)
    public BankTransaction transfer(TransferRequest request) {

        // 1. Validate accounts
        if (request.getFromAccount().equals(request.getToAccount())) {
            throw new BankException(
                "Cannot transfer to the same account");
        }

        if (request.getAmount() == null ||
            request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Invalid transfer amount");
        }

        customerRepository.getByAccountNumber(
            request.getFromAccount()
        ).orElseThrow(() ->
            new AccountNotFoundException("Sender account not found"));

        customerRepository.getByAccountNumber(
            request.getToAccount()
        ).orElseThrow(() ->
            new AccountNotFoundException("Receiver account not found"));

        // 2. Debit sender account
        int debited = customerRepository.debit(
            request.getFromAccount(),
            request.getAmount()
        );

        if (debited == 0) {
            throw new InsufficientBalanceException(
                "Insufficient account balance");
        }

        // 3. Simulate failure for rollback testing
        if (request.isSimulateFailure()) {
            throw new BankException(
                "Simulated system failure after withdrawal");
        }

        // 4. Credit receiver account
        int credited = customerRepository.credit(
            request.getToAccount(),
            request.getAmount()
        );

        if (credited == 0) {
            throw new AccountNotFoundException(
                "Receiver account not found");
        }

        // 5. Save successful transaction
        BankTransaction transaction = new BankTransaction();

        transaction.setAmount(request.getAmount());
        transaction.setFromAccount(request.getFromAccount());
        transaction.setToAccount(request.getToAccount());
        transaction.setDescription("Transfer completed");
        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionRepository.save(transaction);
    }

    // Create a new customer
    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(
                customer.getAccountNumber()) > 0) {
            throw new BankException("Account number already exists");
        }

        if (customerRepository.countEmails(
                customer.getEmail()) > 0) {
            throw new BankException("Email already exists");
        }

        return customerRepository.save(customer);
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    // Search customers by name
    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.searchByName(keyword);
    }

    // Get total bank balance
    public BigDecimal getTotalBalance() {
        return customerRepository.getTotalBalance();
    }

    // Get total customer count
    public long getCustomerCount() {
        return customerRepository.count();
    }

    // Get latest transactions
    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    // Get account transaction history
    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }
}
