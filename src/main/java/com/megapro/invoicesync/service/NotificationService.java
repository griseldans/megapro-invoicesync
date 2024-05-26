package com.megapro.invoicesync.service;

import java.util.List;
import java.util.UUID;

import com.megapro.invoicesync.dto.response.NotificationResponseDTO;
import com.megapro.invoicesync.model.Employee;

public interface NotificationService {
    void generateInvoiceMakerNotification(String employeeEmail, UUID invoiceId);
    void generateInvoiceApproverNotification(String employeeEmail, UUID invoiceId, int approvalId);
    List<List<NotificationResponseDTO>> getEmployeeNotification(Employee employee);
}
