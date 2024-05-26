package com.megapro.invoicesync.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class InvoicePerMonthDTO {
    private String month;
    private int count;
}
