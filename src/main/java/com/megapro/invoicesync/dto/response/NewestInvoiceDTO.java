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
public class NewestInvoiceDTO {
    private String invoiceNumber;
    private String invoiceDate; // Representing the date as a string for simplicity
    private String customerName; // To prevent NullPointerException
    private double grandTotal;
}
