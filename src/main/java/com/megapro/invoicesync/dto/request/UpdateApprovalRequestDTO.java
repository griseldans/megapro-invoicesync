package com.megapro.invoicesync.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApprovalRequestDTO {
    private int approvalId;
    private String comment;
}
