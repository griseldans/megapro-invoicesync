package com.megapro.invoicesync.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

import com.megapro.invoicesync.dto.response.ReadFileResponseDTO;
import com.megapro.invoicesync.dto.response.ReadTaxResponseDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInvoiceRequestDTO {
    private LocalDate invoiceDate = LocalDate.now();
    private LocalDate dueDate;
    private String invoiceNumber;
    private String totalWords;
    private String signature;
    private String city;
    private BigDecimal subtotal;
    private int totalDiscount = 0;
    private List<ReadTaxResponseDTO> listTax;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private String accountNumber;
    private String bankName;
    private String accountName;
    private String status;
    private UUID customerId;
    private String staffEmail;
    private List<ReadFileResponseDTO> filesDTO;
}
