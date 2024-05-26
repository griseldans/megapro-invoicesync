package com.megapro.invoicesync.service;

import com.megapro.invoicesync.repository.ApprovalDb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megapro.invoicesync.dto.InvoiceMapper;
import com.megapro.invoicesync.dto.response.ReadInvoiceResponse;
import com.megapro.invoicesync.model.Approval;
import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.UserApp;

import java.util.ArrayList;
import java. util.List;

@Transactional
@Service
public class ApprovalServiceImpl implements ApprovalService{
    @Autowired
    private ApprovalDb approvalDb;

    @Autowired
    private InvoiceMapper invoiceMapper;
    
    @Override
    public List<Approval> findApproversByInvoice(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice must not be null");
        }
        return approvalDb.findByInvoice(invoice);
    }

    @Override
    public Approval findApprovalByApprovalId(int id) {
        return approvalDb.findByApprovalId(id);
    }

    @Override
    public void saveApproval(Approval approval) {
        approvalDb.save(approval);
    }

    @Override
    public List<UserApp> getEligibleApproversForInvoice(Invoice invoice) {
        throw new UnsupportedOperationException("Unimplemented method 'getEligibleApproversForInvoice'");
    }

    @Override
    public void resetApproval(int approvalId) {
        var approval = approvalDb.findByApprovalId(approvalId);
        approval.setCycle(-1);
        approvalDb.save(approval);
    }

    @Override
    public List<List<ReadInvoiceResponse>> getEmployeeApprovalInvoice(Employee employee) {
        var approvals = employee.getListApproval();
        List<ReadInvoiceResponse> needApproval = new ArrayList<>();
        List<ReadInvoiceResponse> previousApproval = new ArrayList<>();
        
        for(Approval approval:approvals){
            if(approval.isShown() == true){
                var invoice = approval.getInvoice();
    
                ReadInvoiceResponse invoiceDTO = invoiceMapper.readInvoiceToInvoiceResponse(invoice);
                invoiceDTO.setApprovalId(approval.getApprovalId());
                invoiceDTO.setApprovalStatus(approval.getApprovalStatus());
                if(approval.getApprovalStatus().equals("Need Approval")){
                    needApproval.add(invoiceDTO);
                } else {
                    previousApproval.add(invoiceDTO);
                }
            }
        }

        List<List<ReadInvoiceResponse>> allApprovals= new ArrayList<>();
        allApprovals.add(needApproval);
        allApprovals.add(previousApproval);
        
        return allApprovals;
    }
}
