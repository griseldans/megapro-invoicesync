package com.megapro.invoicesync.dto.request;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApprovalRequestDTO {
    private UUID employeeId;
    private UUID invoiceId;
    private int rank;
    private int cycle;
    private String approvalStatus;
    private String comment;
    private boolean shown;
}
