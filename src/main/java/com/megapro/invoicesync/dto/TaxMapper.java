package com.megapro.invoicesync.dto;

import org.mapstruct.Mapper;

import com.megapro.invoicesync.dto.request.CreateTaxRequestDTO;
import com.megapro.invoicesync.dto.response.ReadTaxResponseDTO;
import com.megapro.invoicesync.model.Tax;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxMapper {
    Tax createTaxRequestToTax(CreateTaxRequestDTO createTaxRequest);
    List<ReadTaxResponseDTO> taxListToReadTaxResponseDTOList(List<Tax> taxList);
}
