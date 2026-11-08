package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {

        if (customerRepository.count() == 0) {

            Customer kamal = new Customer();
            kamal.setName("Kamal");
            kamal.setAccountNumber("100001");
            kamal.setEmail("kamal@gmail.com");
            kamal.setBalance(new BigDecimal("50000.00"));

            Customer nimali = new Customer();
            nimali.setName("Nimali");
            nimali.setAccountNumber("100002");
            nimali.setEmail("nimali@gmail.com");
            nimali.setBalance(new BigDecimal("25000.00"));

            Customer saman = new Customer();
            saman.setName("Saman");
            saman.setAccountNumber("100003");
            saman.setEmail("saman@gmail.com");
            saman.setBalance(new BigDecimal("10000.00"));

            customerRepository.save(kamal);
            customerRepository.save(nimali);
            customerRepository.save(saman);

            System.out.println("Sample data inserted!");
            System.out.println("Total balance: LKR 85,000.00");
        }
    }
}