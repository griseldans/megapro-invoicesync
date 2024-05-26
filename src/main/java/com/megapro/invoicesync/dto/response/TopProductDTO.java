package com.megapro.invoicesync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class TopProductDTO {
    private String productName;
    private BigDecimal invoiceCount;
}
