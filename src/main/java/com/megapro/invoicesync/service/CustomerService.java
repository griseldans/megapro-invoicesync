package com.megapro.invoicesync.service;

import com.megapro.invoicesync.model.Customer;
import java.util.List;
import java.util.UUID;

public interface CustomerService {
    void createCustomer(Customer customer);
    List<Customer> getAllCustomer();
    Customer getCustomerById(UUID id);
}