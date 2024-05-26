package com.megapro.invoicesync.service;

import java.io.IOException;
import jakarta.transaction.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.megapro.invoicesync.dto.request.ChangePasswordRequestDTO;
import com.megapro.invoicesync.dto.request.CreateEmployeeRequestDTO;
import com.megapro.invoicesync.dto.request.CreateUserAppRequestDTO;
import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.UserApp;
import com.megapro.invoicesync.repository.EmployeeDb;
import com.megapro.invoicesync.repository.NotificationDb;
import com.megapro.invoicesync.repository.UserAppDb;

@Service
@Transactional
public class UserServiceImpl implements UserService{
    @Autowired
    private UserAppDb userDb;

    @Autowired
    private EmployeeDb employeeDb;

    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    BCryptPasswordEncoder encoder;

    @Autowired
    NotificationDb notificationDb;

    @Override
    public void createUserApp(UserApp user, CreateUserAppRequestDTO userDTO) {
        user.setRole(roleService.getRoleByRoleName(userDTO.getRole().getRole()));
        String hashedPass = encoder.encode(user.getPassword());
        user.setPassword(hashedPass);
        userDb.save(user);
    }

    @Override
    public void createEmployee(Employee employee) {
        String hashedPass = encoder.encode(employee.getPassword());
        employee.setPassword(hashedPass);
        employeeDb.save(employee);
    }

    @Override
    public Employee findByEmail(String email) {
        if (employeeDb.findByEmail(email).isDeleted() != true) {
            return employeeDb.findByEmail(email);
        } 
        return null;
    }

    @Override
    public List<Employee> getAllEmployee() {
        return employeeDb.findByDeletedFalse();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDb.existsByEmail(email);
    }

    @Override
    public boolean existsByNomorHp(String phoneNumber) {
        return employeeDb.existsByNomorHp(phoneNumber);
    }

    @Override
    public Employee getEmployeeById(UUID id) {
        return employeeDb.findEmployeeByUserId(id);
    }

    @Override
    public void deleteEmployee(Employee employee) {
        employee.setDeleted(true);
        employeeDb.save(employee);
    }

    @Override
    public void transferDataEmployee(CreateEmployeeRequestDTO employeeDTO, Employee employee){
        employeeDTO.setEmail(employee.getEmail());
        employeeDTO.setFirst_name(employee.getFirst_name());
        employeeDTO.setLast_name(employee.getLast_name());
        employeeDTO.setNomorHp(employee.getNomorHp());
        employeeDTO.setCountry(employee.getCountry());
        employeeDTO.setCity(employee.getCity());
        employeeDTO.setPostCode(employee.getPostCode());
        employeeDTO.setStreet(employee.getStreet());
        employeeDTO.setRole(employee.getRole());
    }

    @Override
    public Employee updateEmployee(CreateEmployeeRequestDTO employeeDTO){
        Employee employee = employeeDb.findByEmail(employeeDTO.getEmail());
        employee.setFirst_name(employeeDTO.getFirst_name());
        employee.setLast_name(employeeDTO.getLast_name());
        employee.setCountry(employeeDTO.getCountry());
        employee.setNomorHp(employeeDTO.getNomorHp());
        employee.setCity(employeeDTO.getCity());
        employee.setPostCode(employeeDTO.getPostCode());
        employee.setStreet(employeeDTO.getStreet());

        return employee;
    }

    @Override
    public byte[] cekFile(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        
        if (!isImage(file)) {
            throw new IllegalArgumentException("File is not an image");
        }


        return file.getBytes();
    }

    private boolean isImage(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"));
    }

    @Override
    public void savePhoto (Employee employee, byte[] content){
        employee.setImage(content);
    }

    @Override
    public String getImageSrc(Employee employee) {
        if (employee.getImage() != null && employee.getImage().length > 0) {
            return "'data:image/jpeg;base64, '" + Base64.getEncoder().encodeToString(employee.getImage());
        } else {
            return employee.getBase64Photo(); // Assuming getBase64Photo() returns a valid base64 string
        }
    }

    @Override
    public void changePassword(Employee employee, ChangePasswordRequestDTO passwordDTO){
        String hashedPass = encoder.encode(passwordDTO.getNewPassword());
        employee.setPassword(hashedPass);
    }

    @Override
    public boolean checkPass(String newPassword, String existPassword){
        return encoder.matches(newPassword, existPassword);
    }

    @Override 
    public boolean isPassValid(String password){
        return password.length() >= 8 && password.matches(".*\\d.*");
    }

    @Override
    public boolean cekNomorHpExist(String phoneNumber, String email) {

        boolean cek = false;

        for (Employee emp : getAllEmployee()) {
            String empNomorHp = emp.getNomorHp();
            if (empNomorHp != null && empNomorHp.equals(phoneNumber) && !emp.getEmail().equals(email)) {
                cek = true;
                break; 
            }
        }
        return cek;
    }
}
