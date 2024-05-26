package com.megapro.invoicesync.dto;

import java.time.format.DateTimeFormatter;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.megapro.invoicesync.dto.request.UpdateApprovalRequestDTO;
import com.megapro.invoicesync.dto.response.ReadApprovalResponseDTO;
import com.megapro.invoicesync.model.Approval;

@Mapper(componentModel = "spring")
public interface ApprovalMapper {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    UpdateApprovalRequestDTO approvalToUpdateApprovalRequestDTO(Approval approval);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeRole", ignore = true)
    @Mapping(target = "approvalDate", ignore = true)
    ReadApprovalResponseDTO approvalToReadApprovalResponseDTO(Approval approval);

    @AfterMapping
    default void fillEmployeeInfo(@MappingTarget ReadApprovalResponseDTO approvalDTO, Approval approval){
        approvalDTO.setEmployeeName(approval.getEmployee().getFirst_name()+ " " + approval.getEmployee().getLast_name());
        approvalDTO.setEmployeeRole(approval.getEmployee().getRole().getRole());

        var today = approval.getApprovalTime();
        String formattedDate = today.format(formatter);
        approvalDTO.setApprovalDate(formattedDate);
    }
}
