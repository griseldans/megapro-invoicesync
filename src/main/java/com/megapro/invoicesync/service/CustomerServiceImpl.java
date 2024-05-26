package com.megapro.invoicesync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.megapro.invoicesync.model.Customer;
import com.megapro.invoicesync.repository.CustomerDb;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService{
    @Autowired
    CustomerDb customerDb;

    @Override
    public void createCustomer(Customer customer) {
        customerDb.save(customer);
    }

    @Override
    public Customer getCustomerById(UUID id){
        System.out.println("customer id di service " + id);
        return customerDb.findByCustomerId(id);
    }

    @Override
    public List<Customer> getAllCustomer() {
        return customerDb.findAll();
    }
}
