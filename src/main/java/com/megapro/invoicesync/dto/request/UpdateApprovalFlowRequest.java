package com.megapro.invoicesync.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateApprovalFlowRequest {
    private int approvalRank;
    @NotEmpty(message = "Approver role cannot be empty")
    private String approverRole;
    private long nominalRange;
}
