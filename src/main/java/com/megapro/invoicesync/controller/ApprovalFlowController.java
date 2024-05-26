package com.megapro.invoicesync.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.megapro.invoicesync.dto.ApprovalFlowMapper;
import com.megapro.invoicesync.dto.request.CreateApprovalFlowRequest;
import com.megapro.invoicesync.dto.request.UpdateApprovalFlowRequest;
import com.megapro.invoicesync.model.ApprovalFlow;
import com.megapro.invoicesync.model.Role;
import com.megapro.invoicesync.repository.UserAppDb;
import com.megapro.invoicesync.service.ApprovalFlowService;
import com.megapro.invoicesync.service.RoleService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
public class ApprovalFlowController {

    @Autowired
    ApprovalFlowService approvalFlowService;

    @Autowired 
    ApprovalFlowMapper approvalFlowMapper;

    @Autowired
    UserAppDb userAppDb;

    @Autowired
    RoleService roleService;

    @PostMapping(value="/add-approval-flow")
    public String createFlow(@Valid CreateApprovalFlowRequest approvalFlowDTO, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        if (result.hasErrors()) {
            // Ekstrak pesan error dari hasil validasi
            String errorMessage = result.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/approval-flows";
        }
        try {
            var approvalFlow = approvalFlowMapper.createApprovalFlowRequestToApprovalFlow(approvalFlowDTO);
            approvalFlowService.createApprovalFlow(approvalFlow);
            redirectAttributes.addFlashAttribute("successMessage", "Flow added successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/approval-flows";
        }

        return "redirect:/approval-flows";
    }
    
    
    @GetMapping("/approval-flows")
    public String getAllFlow(Model model, @ModelAttribute("successMessage") String successMessage,
                                @ModelAttribute("errorMessage") String errorMessage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        model.addAttribute("role", role);
        model.addAttribute("email", email);                         
        List<Role> roles = roleService.getAllRole();
        roles = roles.stream()
        .filter(r -> !r.getRole().equalsIgnoreCase("Admin"))
        .collect(Collectors.toList());
        List<ApprovalFlow> listApproval = approvalFlowService.getAllApprovalFlows();
        listApproval.sort(Comparator.comparingLong(ApprovalFlow::getNominalRange));
        model.addAttribute("approvalFlow", new CreateApprovalFlowRequest());        
        model.addAttribute("listApproval", (listApproval != null) ? listApproval : new ArrayList<>());
        model.addAttribute("roles", roles);
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);
         // Sorting by nominal range for consistent display order
        return "approve-invoice/approval-hierarchy";
    }

    @GetMapping("/reset-approval-flows")
    public String resetApprovalFlows(RedirectAttributes redirectAttributes) {
        try {
            approvalFlowService.resetAllApprovalFlows(); // Metode untuk menghapus seluruh approval flow
            redirectAttributes.addFlashAttribute("successMessage", "All flows deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "There is an error while deleting the flow");
        }
        return "redirect:/approval-flows";
    }

    @PostMapping("/update-approval-flow/{approvalRank}")
    public String updateFlow(@PathVariable int approvalRank,
                            @Valid UpdateApprovalFlowRequest approvalFlowDTO, 
                            BindingResult result, 
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/approval-flows";
        }
        try {
            approvalFlowService.updateApprovalFlow(approvalRank, approvalFlowDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Flow updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/approval-flows";
    }



}
