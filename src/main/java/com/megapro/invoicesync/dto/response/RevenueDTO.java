package com.megapro.invoicesync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class RevenueDTO {
    private String month;
    private BigDecimal revenue;

    public String toString() {
        return String.format("[\"%s\", %.2f]", month, revenue.doubleValue()); 
    }
}
