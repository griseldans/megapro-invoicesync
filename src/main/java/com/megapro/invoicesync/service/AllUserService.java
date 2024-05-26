package com.megapro.invoicesync.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.UserApp;
import com.megapro.invoicesync.repository.EmployeeDb;
import com.megapro.invoicesync.repository.UserAppDb;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AllUserService {
    @Autowired
    private EmployeeDb employeeDB;

    @Autowired
    private UserAppDb userAppDB;

    public void createUserApp(UserApp userApp){
        userAppDB.save(userApp);
    }

    public void createEmployee(Employee employee){
        employeeDB.save(employee);
    }

    public Employee findByEmail(String email){
        return employeeDB.findByEmail(email);
    }

    public List<Employee> getAllEmployee(){
        return employeeDB.findAll();
    }


}
