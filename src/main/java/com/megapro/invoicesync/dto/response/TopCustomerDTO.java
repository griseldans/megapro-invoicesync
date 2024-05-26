package com.megapro.invoicesync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TopCustomerDTO {
    private String customerName;
    private Long invoiceCount;
}
