package com.megapro.invoicesync.service;

import com.megapro.invoicesync.dto.request.CreateInvoiceRequestDTO;
import com.megapro.invoicesync.dto.request.UpdateInvoiceRequestDTO;
import com.megapro.invoicesync.dto.response.ApproverDisplay;
import com.megapro.invoicesync.model.Approval;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.Product;
import com.megapro.invoicesync.model.UserApp;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public interface InvoiceService {
    void createInvoice(Invoice invoice, String email);
    void attributeInvoice(Invoice invoice, List<Integer> listTax);
    long countInvoice();
    Invoice getInvoiceById(UUID id);
    List<Invoice> retrieveAllInvoice();
    List<Invoice> retrieveInvoicesByRole(String role);
    List<Invoice> retrieveInvoicesByEmail(String email);
    List<Invoice> retrieveInvoicesByDivision(String division);
    List<Invoice> getInvoiceByStaffEmail(String email);
    List<Product> getListProductInvoice(Invoice invoice);
    List<Invoice> retrieveInvoicesByEmailAndStatus(String email, String status);
    List<Invoice> retrieveInvoicesByDivisionAndStatus(String division, String status);
    List<Invoice> retrieveInvoicesByStatus(String status);
    Invoice getDummyInvoice();
    String translateByte(byte[] byteFile);
    Invoice getInvoiceByInvoiceNumber(String invoiceNumber);
    String parseDate(LocalDate localDate);
    String checkValidity(CreateInvoiceRequestDTO invoiceDTO, List<Integer> selectedTaskIds, String email);
    String checkValidityUpdate(UpdateInvoiceRequestDTO invoiceDTO);
    List<ApproverDisplay> getApproverDisplaysForInvoice(Invoice invoice);
    Invoice updateInvoice(Invoice invoiceFromDTO, List<Integer> selectedTaxIds);
    void addApproverToInvoice(UUID invoiceId, String email);
    Approval readdApproverToInvoice(UUID invoiceId, String email, int oldSize);
    List<UserApp> getEligibleApproversForInvoice(Invoice invoice);
    void save(Invoice invoice);
}
