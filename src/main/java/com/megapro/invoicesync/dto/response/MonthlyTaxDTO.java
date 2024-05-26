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
public class MonthlyTaxDTO {
    private int month; // Month number (1 for January, 2 for February, etc.)
    private double totalTax; // Total tax for the month
    
}
