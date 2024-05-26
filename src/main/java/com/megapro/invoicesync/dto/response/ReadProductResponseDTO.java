package com.megapro.invoicesync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;
import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class ReadProductResponseDTO {
    private UUID productId;
    private String description;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
