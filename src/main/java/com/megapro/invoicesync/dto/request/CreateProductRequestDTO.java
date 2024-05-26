package com.megapro.invoicesync.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequestDTO {
    private String description;
    private String quantity;
    private String price;
    private String totalPrice;
}