package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    // Dashboard - HTML
    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("customers",
                    bankService.searchCustomers(keyword));
        } else {
            model.addAttribute("customers",
                    bankService.getAllCustomers());
        }

        model.addAttribute("transactions",
                bankService.getLatestTransactions());

        model.addAttribute("totalBalance",
                bankService.getTotalBalance());

        model.addAttribute("customerCount",
                bankService.getCustomerCount());

        model.addAttribute("keyword", keyword);

        return "bank";
    }

    // Add Customer - RAW JSON
    @PostMapping("/customers")
    @ResponseBody
    public ResponseEntity<?> createCustomer(
            @RequestBody Customer customer) {

        try {
            Customer savedCustomer =
                    bankService.createCustomer(customer);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Customer added successfully",
                            "customer", savedCustomer
                    ));

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // Transfer Money - RAW JSON
    @PostMapping("/transfer")
    @ResponseBody
    public ResponseEntity<?> transfer(
            @Valid @RequestBody TransferRequest request,
            BindingResult result) {

        // Validation errors
        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            result.getFieldError().getDefaultMessage()
                    ));
        }

        try {
            BankTransaction transaction =
                    bankService.transfer(request);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Transfer successful",
                            "transaction", transaction
                    )
            );

        } catch (RuntimeException ex) {

            auditService.recordFailed(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    ex.getMessage()
            );

            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // Account History - HTML
    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        model.addAttribute("accountNumber", accountNumber);

        model.addAttribute("transactions",
                bankService.getAccountHistory(accountNumber));

        return "history";
    }
}

