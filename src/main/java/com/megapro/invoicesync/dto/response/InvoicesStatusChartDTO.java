package com.megapro.invoicesync.dto.response;
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
public class InvoicesStatusChartDTO {
    private String status; // e.g., "Paid", "Approved"
    private int count;
    
}
