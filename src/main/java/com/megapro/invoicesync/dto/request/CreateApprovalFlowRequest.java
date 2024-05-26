package com.megapro.invoicesync.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateApprovalFlowRequest {
    @NotEmpty(message = "Approver role tidak boleh kosong")
    private String approverRole;
    private long nominalRange;
}
    
