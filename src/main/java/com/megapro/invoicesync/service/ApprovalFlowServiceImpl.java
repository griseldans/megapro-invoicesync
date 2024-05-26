package com.megapro.invoicesync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megapro.invoicesync.dto.ApprovalFlowMapper;
import com.megapro.invoicesync.dto.request.CreateApprovalRequestDTO;
import com.megapro.invoicesync.dto.request.UpdateApprovalFlowRequest;
import com.megapro.invoicesync.model.ApprovalFlow;
import com.megapro.invoicesync.repository.ApprovalDb;
import com.megapro.invoicesync.repository.ApprovalFlowDb;
import com.megapro.invoicesync.repository.EmployeeDb;
import com.megapro.invoicesync.repository.InvoiceDb;

import java.util.Comparator;
import java.util.List;

@Transactional
@Service
public class ApprovalFlowServiceImpl implements ApprovalFlowService {

    @Autowired
    private ApprovalFlowDb approvalFlowDb;

    @Override
    public void createApprovalFlow(ApprovalFlow approvalFlow) throws IllegalArgumentException {
        // Cek apakah ada approval flow yang sudah ada
        List<ApprovalFlow> existingFlows = approvalFlowDb.findAll();
        
        // Sort existing flows berdasarkan nominal range secara ascending
        existingFlows.sort(Comparator.comparingLong(ApprovalFlow::getNominalRange));
        
        if (approvalFlow.getNominalRange() <= 0) {
            throw new IllegalArgumentException("The nominal value must be positive.");
        }
        // Check if the employee is already included in any previous flows
        if (existingFlows.stream().anyMatch(flow -> flow.getApproverRole().equals(approvalFlow.getApproverRole()))) {
            throw new IllegalArgumentException("Employee for the role " + approvalFlow.getApproverRole() + " has already been assigned to a previous flow");
        }

        // Jika ada existing flow, pastikan nominal baru lebih besar dari nominal terakhir
        if (!existingFlows.isEmpty()) {
            ApprovalFlow lastFlow = existingFlows.get(existingFlows.size() - 1);
            if (approvalFlow.getNominalRange() <= lastFlow.getNominalRange()) {
                throw new IllegalArgumentException("The nominal value of the new flow must be greater than the previous flow");
            }
        }

        approvalFlowDb.save(approvalFlow);
    }

    @Override
    public List<ApprovalFlow> getAllApprovalFlows() {
        return approvalFlowDb.findAll();
    }

    @Override
    @Transactional
    public void resetAllApprovalFlows() {
        approvalFlowDb.deleteAll();
    }

    @Override
    @Transactional
    public void updateApprovalFlow(int approvalRank, UpdateApprovalFlowRequest flowDTO) throws IllegalArgumentException {
        ApprovalFlow existingFlow = approvalFlowDb.findById(approvalRank)
            .orElseThrow(() -> new IllegalArgumentException("Approval flow not found"));
    
        if (flowDTO.getNominalRange() <= 0) {
            throw new IllegalArgumentException("The nominal value must be positive.");
        }
    
        List<ApprovalFlow> allFlows = approvalFlowDb.findAll();
        allFlows.remove(existingFlow);  // Remove the current flow for re-validation
    
        // Check nominal ranges
        for (ApprovalFlow flow : allFlows) {
            if (flowDTO.getNominalRange() <= flow.getNominalRange() && flow.getApprovalRank() < existingFlow.getApprovalRank()) {
                throw new IllegalArgumentException("The nominal value must be greater than all previous flows.");
            }
            if (flowDTO.getNominalRange() >= flow.getNominalRange() && flow.getApprovalRank() > existingFlow.getApprovalRank()) {
                throw new IllegalArgumentException("The nominal value must be less than all subsequent flows.");
            }
        }
    
        // Check role uniqueness but exclude the current flow from the check
        if (allFlows.stream().anyMatch(flow -> flow.getApproverRole().equals(flowDTO.getApproverRole()) && !flow.equals(existingFlow))) {
            throw new IllegalArgumentException("Employee for the role " + flowDTO.getApproverRole() + " is already assigned in another flow.");
        }
    
        existingFlow.setApproverRole(flowDTO.getApproverRole());
        existingFlow.setNominalRange(flowDTO.getNominalRange());
    
        // Re-add the updated flow and re-sort
        allFlows.add(existingFlow);
        allFlows.sort(Comparator.comparingLong(ApprovalFlow::getNominalRange));
    
        // Save all flows to maintain the sorted order
        for (ApprovalFlow flow : allFlows) {
            approvalFlowDb.save(flow);
        }
    }
    
    
    
    
    
    
    


    // Coba untuk restcontroller approver (coba-coba)
    @Autowired
    ApprovalDb approvalDb;

    @Autowired
    ApprovalFlowMapper approvalFlowMapper;

    @Autowired
    UserService userService;

    @Autowired
    EmployeeDb employeeDb;

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    InvoiceDb invoiceDb;

    @Override
    public void saveApprover(List<CreateApprovalRequestDTO> createApprovalDTOList){
        for(CreateApprovalRequestDTO approvalDTO:createApprovalDTOList){
            var approval = approvalFlowMapper.CreateApprovalRequestDTOToApproval(approvalDTO);
            var employee = employeeDb.findEmployeeByUserId(approvalDTO.getEmployeeId());
            var invoice = invoiceService.getInvoiceById(approvalDTO.getInvoiceId());
            approval.setEmployee(employee);
            approval.setInvoice(invoice);
            approval.setApprovalStatus("Need Approval");
            approvalDb.save(approval);
            System.out.println("Hai ini masuk sini");
            invoice.setStatus("Pending Approval");
            invoiceDb.save(invoice);
        }
    }

}
