package com.megapro.invoicesync.dto.response;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OutboundInvoiceCountDTO {
    private String monthName; // e.g., "January 2023"
    private int invoiceCount;
    
}
