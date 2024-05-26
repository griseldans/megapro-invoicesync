package com.megapro.invoicesync.service;

import java.util.List;

import com.megapro.invoicesync.dto.request.CreateApprovalRequestDTO;
import com.megapro.invoicesync.dto.request.UpdateApprovalFlowRequest;
import com.megapro.invoicesync.model.ApprovalFlow;

public interface ApprovalFlowService {
    void createApprovalFlow(ApprovalFlow approvalFlow);
    List<ApprovalFlow> getAllApprovalFlows();
    void resetAllApprovalFlows();

    // coba-coba delete soon
    void saveApprover(List<CreateApprovalRequestDTO> createApprovalDTOList);
    void updateApprovalFlow(int approvalRank, UpdateApprovalFlowRequest flowDTO);
}
