package com.megapro.invoicesync.service;

import java.util.List;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.megapro.invoicesync.dto.request.ChangePasswordRequestDTO;
import com.megapro.invoicesync.dto.request.CreateEmployeeRequestDTO;
import com.megapro.invoicesync.dto.request.CreateUserAppRequestDTO;
import com.megapro.invoicesync.dto.response.ReadInvoiceResponse;
import com.megapro.invoicesync.model.Approval;
import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.UserApp;

public interface UserService {
    void createUserApp(UserApp user, CreateUserAppRequestDTO userDTO);
    void createEmployee(Employee employee);
    Employee findByEmail(String email);
    List<Employee> getAllEmployee();
    boolean existsByEmail(String email);
    boolean existsByNomorHp(String phoneNumber);
    Employee getEmployeeById(UUID id);
    void deleteEmployee(Employee employee);
    void transferDataEmployee(CreateEmployeeRequestDTO employeeDTO, Employee employee);
    Employee updateEmployee(CreateEmployeeRequestDTO employeeDTO);
    byte[] cekFile(MultipartFile file) throws IOException;
    void savePhoto (Employee employee, byte[] content);
    String getImageSrc(Employee employee);
    void changePassword(Employee employee, ChangePasswordRequestDTO passwordDTO);
    boolean checkPass(String password, String oldPassword);
    boolean isPassValid(String password);
    boolean cekNomorHpExist(String phoneNumber, String email);
}
