package com.megapro.invoicesync.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateTaxRequestDTO {
    private String taxName;
    private int taxPercentage;
}
