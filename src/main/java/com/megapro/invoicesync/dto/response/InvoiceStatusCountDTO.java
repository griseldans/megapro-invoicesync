package com.megapro.invoicesync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class InvoiceStatusCountDTO {
    private String status;
    private Long count;
}
