package com.megapro.invoicesync.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import com.megapro.invoicesync.dto.UserMapper;
import com.megapro.invoicesync.dto.request.ChangePasswordRequestDTO;
import com.megapro.invoicesync.dto.request.CreateEmployeeRequestDTO;
import com.megapro.invoicesync.model.Role;
import com.megapro.invoicesync.repository.EmployeeDb;
import com.megapro.invoicesync.repository.UserAppDb;
import com.megapro.invoicesync.service.UserService;

import jakarta.validation.Valid;

import com.megapro.invoicesync.service.NotificationService;
import com.megapro.invoicesync.service.RoleService;
import org.springframework.web.server.ResponseStatusException;


@Controller
public class EmployeeController {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    UserAppDb userAppDb;

    @Autowired
    EmployeeDb employeeDb;

    @Autowired
    NotificationService notificationService;
    

    @GetMapping("/create-account")
    public String formCreateEmployee(Model model, @ModelAttribute("successMessage") String successMessage,
    @ModelAttribute("errorMessage") String errorMessage){
    
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        model.addAttribute("email", email);
        model.addAttribute("role", role);

        var employeeDTO = new CreateEmployeeRequestDTO();
        var listRole = roleService.getAllRole();
        model.addAttribute("employeeDTO", employeeDTO);
        model.addAttribute("listRole", listRole);
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);

        return "account/form-create-account";
    }
    
    @PostMapping("/create-account")
    public String createEmployeeAccount(@Valid CreateEmployeeRequestDTO employeeDTO, BindingResult result, Model model, RedirectAttributes redirectAttributes,
    @ModelAttribute("successMessage") String successMessage, @ModelAttribute("errorMessage") String errorMessage){
        var employee = userMapper.createEmployeeRequestDTOToEmployee(employeeDTO);

        Long roleId = employeeDTO.getRole().getId();
        Role role = roleService.getRoleByRoleId(roleId);
        employee.setRole(role);

        // Check for duplicate email
        if (userService.existsByEmail(employeeDTO.getEmail())) {
            var newEmployeeDTO = new CreateEmployeeRequestDTO();
            var listRole = roleService.getAllRole();
            model.addAttribute("employeeDTO", newEmployeeDTO);
            model.addAttribute("listRole", listRole);
            model.addAttribute("successMessage", successMessage);
            model.addAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("errorMessage", "Email already exists. Please choose a different email.");
            return "redirect:/create-account";
        }

        // Check for duplicate phone number
        if (userService.existsByNomorHp(employeeDTO.getNomorHp())) {
            var newEmployeeDTO = new CreateEmployeeRequestDTO();
            var listRole = roleService.getAllRole();
            model.addAttribute("employeeDTO", newEmployeeDTO);
            model.addAttribute("listRole", listRole);
            model.addAttribute("successMessage", successMessage);
            model.addAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("errorMessage", "Phone number already exists. Please choose a different phone number.");
            return "redirect:/create-account";
        }

        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Please check your input");
            return "account/form-create-account"; 
        }
        
        employee.setNomorHp(employeeDTO.getNomorHp());
        userService.createEmployee(employee);
        
        model.addAttribute("employeeEmail", employee.getEmail());
        model.addAttribute("employee", employeeDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Employee's account successfully registered.");

        return "redirect:/create-account";
    }
    
    
    @GetMapping("/employees")
    public String viewAllEmployee(Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        model.addAttribute("email", email);
        model.addAttribute("role", role);

        var listEmployee = userService.getAllEmployee();
        model.addAttribute("listEmployee", listEmployee);

        return "account/viewall-employee";
    }
    
    // Delete employee
    @GetMapping("employee/{id}/delete")
    public String deleteEmployee(@PathVariable("id") UUID id, Model model) {
        var employee = userService.getEmployeeById(id);
        userService.deleteEmployee(employee);
        // model.addAttribute("id", id);
        return "redirect:/employees";
    }

    @GetMapping("/profile-page")
    public String getProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        var employee = employeeDb.findByEmail(email);

        Boolean flag = false;

        if (employee.getImage() != null && employee.getImage().length > 0) {
            flag = true;
            var imageBase64 = Base64.getEncoder().encodeToString(employee.getImage());
            model.addAttribute("imageBase64", imageBase64);
        } else {
            model.addAttribute("imageBase64", employee.getBase64Photo());
        }

        System.out.println("NOMOR HP " + employee.getNomorHp());
        
        model.addAttribute("flag", flag);
        model.addAttribute("employee", employee);
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        // Notification
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        return "home/profile-page.html";
    }

    @GetMapping("/profile-page/edit")
    public String editProfile(Model model, @ModelAttribute("successMessage") String successMessage, @ModelAttribute("errorMessage") String errorMessage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        var employee = employeeDb.findByEmail(email);
        CreateEmployeeRequestDTO employeeDT0 = new CreateEmployeeRequestDTO();
        userService.transferDataEmployee(employeeDT0, employee);
        employeeDT0.setRoleString(role);

        Boolean flag = false;

        if (employee.getImage() != null && employee.getImage().length > 0) {
            flag = true;
            var imageBase64 = Base64.getEncoder().encodeToString(employee.getImage());
            model.addAttribute("imageBase64", imageBase64);
        } else {
            model.addAttribute("imageBase64", employee.getBase64Photo());
        }

        model.addAttribute("flag", flag);
        model.addAttribute("employeeDTO", employeeDT0);
        model.addAttribute("employee", employee);
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        // Notification
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        return "home/edit-profile-page.html";
    }
    
    @PostMapping("/profile-page/edit")
    public String postEditProfile(@Valid @ModelAttribute CreateEmployeeRequestDTO employeeDTO, Model model, RedirectAttributes redirectAttributes,
    @ModelAttribute("successMessage") String successMessage, @ModelAttribute("errorMessage") String errorMessage) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        var employee = employeeDb.findByEmail(email);

        byte[] content;

        if(!employeeDTO.getImageFile().isEmpty()){
            try {
                content = userService.cekFile(employeeDTO.getImageFile());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing the file");
            }
            userService.savePhoto(employee, content);
        }

        if (userService.cekNomorHpExist(employeeDTO.getNomorHp(), email) == true) {
            System.out.println("Dupe Nomornya");
            var newEmployeeDTO = new CreateEmployeeRequestDTO();

            Boolean flag = false;

            if (employee.getImage() != null && employee.getImage().length > 0) {
                flag = true;
                var imageBase64 = Base64.getEncoder().encodeToString(employee.getImage());
                model.addAttribute("imageBase64", imageBase64);
            } else {
                model.addAttribute("imageBase64", employee.getBase64Photo());
            }

            model.addAttribute("employeeDTO", newEmployeeDTO);
            model.addAttribute("flag", flag);
            model.addAttribute("employee", employee);
            model.addAttribute("role", role);
            model.addAttribute("successMessage", successMessage);
            model.addAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("errorMessage", "Phone number already exists. Please choose a different phone number.");
            return "redirect:/profile-page/edit";
        }

        Boolean flag = false;

        if (employee.getImage() != null && employee.getImage().length > 0) {
            flag = true;
            var imageBase64 = Base64.getEncoder().encodeToString(employee.getImage());
            model.addAttribute("imageBase64", imageBase64);
        } else {
            model.addAttribute("imageBase64", employee.getBase64Photo());
        }

        var employeeUpdated = userService.updateEmployee(employeeDTO);
        
        model.addAttribute("flag", flag);
        model.addAttribute("employee", employeeUpdated);
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        return "home/profile-page.html";
    }

    @GetMapping("/change-password")
    public String formChangePassword(Model model, @ModelAttribute("successMessage") String successMessage,
    @ModelAttribute("errorMessage") String errorMessage){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        ChangePasswordRequestDTO PasswordDTO = new ChangePasswordRequestDTO();

        model.addAttribute("passwordDTO", PasswordDTO);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);

        // Notification
        var employee = employeeDb.findByEmail(email);
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        return "account/form-change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid ChangePasswordRequestDTO passwordDTO, Model model, RedirectAttributes redirectAttributes,
    @ModelAttribute("successMessage") String successMessage, @ModelAttribute("errorMessage") String errorMessage) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        var employee = employeeDb.findByEmail(email);
        String role = user.getRole().getRole();

        if (userService.checkPass(passwordDTO.getOldPassword(), user.getPassword())) {

            if (!userService.checkPass(passwordDTO.getNewPassword(), user.getPassword())){

                if (userService.isPassValid(passwordDTO.getNewPassword())) {
                    userService.changePassword(employee, passwordDTO);
                    model.addAttribute("role", role);
                    model.addAttribute("successMessage", successMessage);
                    model.addAttribute("errorMessage", errorMessage);
                    redirectAttributes.addFlashAttribute("successMessage", "Succesfully changes your password");
                    return "redirect:/change-password";

                } else {

                    ChangePasswordRequestDTO newPasswordDTO = new ChangePasswordRequestDTO();
                    model.addAttribute("passwordDTO", newPasswordDTO);
                    model.addAttribute("role", role);
                    model.addAttribute("successMessage", successMessage);
                    model.addAttribute("errorMessage", errorMessage);
                    redirectAttributes.addFlashAttribute("errorMessage", "Please Make sure your new password met the requirement");
                    return "redirect:/change-password";
                }


            } else {

                ChangePasswordRequestDTO newPasswordDTO = new ChangePasswordRequestDTO();
                model.addAttribute("passwordDTO", newPasswordDTO);
                model.addAttribute("role", role);
                model.addAttribute("successMessage", successMessage);
                model.addAttribute("errorMessage", errorMessage);
                redirectAttributes.addFlashAttribute("errorMessage", "Your new password cannot be the same as your old password");
                return "redirect:/change-password";

            }

        } else {
         
            ChangePasswordRequestDTO newPasswordDTO = new ChangePasswordRequestDTO();
            model.addAttribute("passwordDTO", newPasswordDTO);
            model.addAttribute("role", role);
            model.addAttribute("successMessage", successMessage);
            model.addAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("errorMessage", "Your old password is incorrect, please input the correct password");
            return "redirect:/change-password";
        }
    }
}