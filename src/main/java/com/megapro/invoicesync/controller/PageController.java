package com.megapro.invoicesync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.megapro.invoicesync.repository.EmployeeDb;
import com.megapro.invoicesync.repository.UserAppDb;
import com.megapro.invoicesync.service.NotificationService;
import com.megapro.invoicesync.service.DashboardService;

@Controller
public class PageController {

    @Autowired
    UserAppDb userAppDb;

    @Autowired
    EmployeeDb employeeDb;

    @Autowired
    NotificationService notificationService;

    @Autowired
    DashboardService dashboardService;

    @GetMapping(value = {"/home"})
    public String home(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        String[] parts = role.split(" ");
        String division = parts[0];

        var employee = employeeDb.findByEmail(email);

        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("division", division);
        model.addAttribute("employee", employee);

        if(!role.equals("Admin")){
            // Notification
            var notifications = notificationService.getEmployeeNotification(employee);
            model.addAttribute("notifications0", notifications.get(0));
            model.addAttribute("notifications1", notifications.get(1));
            model.addAttribute("notifications7", notifications.get(2));
            model.addAttribute("notifications30", notifications.get(3));
        }

        if (role.equals("Non-Finance Staff")) {
            if (employee.getFirst_name() == null) {
                model.addAttribute("showModal", "true");
            } else {
                model.addAttribute("showModal", "false");
            }
            return "home/home-non-finance.html"; // home staf non finance

        } else if (role.equals("Finance Staff")) {
            if (employee.getFirst_name() == null) {
                model.addAttribute("showModal", "true");
            } else {
                model.addAttribute("showModal", "false");
            }
            return "home/home-staff-finance.html";

        } else if (role.equals("Non-Finance Manager")) {
            if (employee.getFirst_name() == null) {
                model.addAttribute("showModal", "true");
            } else {
                model.addAttribute("showModal", "false");
            }
            return "home/home-exc-non-finance.html";

        } else if (role.equals("Finance Director") ) {
            if (employee.getFirst_name() == null) {
                model.addAttribute("showModal", "true");
            } else {
                model.addAttribute("showModal", "false");
            }
            return "home/home-exc-finance.html";
            
        } else if (role.equals("Finance Manager")) {
            if (employee.getFirst_name() == null) {
                model.addAttribute("showModal", "true");
            } else {
                model.addAttribute("showModal", "false");
            }
            return "home/home-manager-finance.html";
            
        } else {
            return "home/home-admin.html";
        }
    }

    @GetMapping("/login")
    public String loginPage(){
        return "auth/login.html";
    }

    // @GetMapping("/create-user-account")
    // public String createUser(Model model){
    //     var userAppDTO = new CreateUserAppRequestDTO();
    //     model.addAttribute("userAppDTO", userAppDTO);
    //     return "auth/create-user-account.html";
    // }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403.html"; 
    }
}
