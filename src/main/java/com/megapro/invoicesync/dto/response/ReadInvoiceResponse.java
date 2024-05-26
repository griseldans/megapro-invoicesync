package com.megapro.invoicesync.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

import com.megapro.invoicesync.model.Customer;
import com.megapro.invoicesync.model.Tax;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReadInvoiceResponse {
    private UUID invoiceId;
    private String staffEmail;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String totalWords;
    private String city;
    // private String productDocument;
    private BigDecimal subtotal;
    private int totalDiscount;
    private List<ReadTaxResponseDTO> listTax;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private String accountNumber;
    private String bankName;
    private String accountName;
    // private String additionalDocument;
    private Customer customer;
    private String status;
    private String staffRole;
    private int approvalId;
    private String approvalStatus;
}
