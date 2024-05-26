package com.megapro.invoicesync.dto;

import org.mapstruct.Mapper;

import com.megapro.invoicesync.dto.request.CreateEmployeeRequestDTO;
import com.megapro.invoicesync.dto.request.CreateUserAppRequestDTO;
import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.UserApp;

@Mapper(componentModel = "spring")
public interface UserMapper {

    Employee createEmployeeRequestDTOToEmployee(CreateEmployeeRequestDTO createEmployeeRequest);
    UserApp createUserAppRequestDTOToUserApp(CreateUserAppRequestDTO createUserAppRequest);
    
}
