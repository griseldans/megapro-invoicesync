package com.megapro.invoicesync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MonthStatusDTO {
    private String month;
    private int paidInvoices;
    private int unpaidInvoices;
}
