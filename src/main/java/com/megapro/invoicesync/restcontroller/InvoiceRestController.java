package com.megapro.invoicesync.restcontroller;

import java.util.List;
import java.util.UUID;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.megapro.invoicesync.dto.ApprovalFlowMapper;
import com.megapro.invoicesync.dto.request.CreateApprovalRequestDTO;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.repository.ApprovalDb;
import com.megapro.invoicesync.service.ApprovalFlowService;
import com.megapro.invoicesync.service.InvoiceService;
import com.megapro.invoicesync.service.PrintService;
import com.megapro.invoicesync.service.UserService;

@RestController
public class InvoiceRestController {
    @Autowired
    ApprovalDb approvalDb;

    @Autowired
    ApprovalFlowMapper approvalMapper;

    @Autowired
    ApprovalFlowService approvalService;

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    UserService userService;

    @Autowired
    PrintService printService;

    @PostMapping("/invoice/add-approver")
    public ResponseEntity<String> addApprover(@RequestBody List<CreateApprovalRequestDTO> approvalListDTO){
        approvalService.saveApprover(approvalListDTO);
        return ResponseEntity.ok("berhasil");
    }

    @GetMapping("/api/v1/invoice/{invoiceId}/download")
    public ResponseEntity<?> downloadInvoice(@PathVariable("invoiceId") UUID invoiceId) throws IOException {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        byte[] pdfBytes = printService.generateInvoice(invoice);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .body(pdfBytes);
    }
}
