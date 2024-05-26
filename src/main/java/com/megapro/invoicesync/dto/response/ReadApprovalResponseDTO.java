package com.megapro.invoicesync.dto.response;

import java.util.List;

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
public class ReadApprovalResponseDTO {
    private String employeeName;
    private String employeeRole;
    private int approvalRank;
    private int cycle;
    private String approvalStatus;
    private String comment;
    private String approvalDate;
    private List<ReadFileResponseDTO> filesDTO;
}
