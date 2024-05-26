package com.megapro.invoicesync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import com.megapro.invoicesync.dto.CustomerMapper;
import com.megapro.invoicesync.dto.request.CreateCustomerRequestDTO;
import com.megapro.invoicesync.repository.UserAppDb;
import com.megapro.invoicesync.service.CustomerService;

import jakarta.validation.Valid;

@Controller
public class CustomerController {
    @Autowired
    UserAppDb userAppDb;

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    @PostMapping("/create-customer")
    public RedirectView createCustomer(@Valid CreateCustomerRequestDTO customerRequest){
        var customer = customerMapper.createCustomerDTOToCustomer(customerRequest);
        customerService.createCustomer(customer);
        return new RedirectView("/create-invoice");
    }
}
