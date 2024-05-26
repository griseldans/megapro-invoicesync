package com.megapro.invoicesync.dto.request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInvoiceRequestDTO extends CreateInvoiceRequestDTO {
    private UUID invoiceId;
}