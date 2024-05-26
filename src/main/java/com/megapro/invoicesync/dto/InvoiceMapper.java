package com.megapro.invoicesync.dto;

import java.util.List;
import java.util.ArrayList;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.megapro.invoicesync.dto.request.CreateInvoiceRequestDTO;
import com.megapro.invoicesync.dto.request.UpdateInvoiceRequestDTO;
import com.megapro.invoicesync.dto.response.ReadInvoiceResponse;
import com.megapro.invoicesync.dto.response.ReadTaxResponseDTO;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.Tax;

@Mapper(componentModel="spring")
public interface InvoiceMapper {

    Invoice createInvoiceRequestToInvoice(CreateInvoiceRequestDTO CreateInvoiceRequest);

    @Mapping(target="listTax", ignore=true)
    ReadInvoiceResponse readInvoiceToInvoiceResponse(Invoice invoice);

    @AfterMapping
    default void configureInvoicesTax(@MappingTarget ReadInvoiceResponse readInvoiceResponse, Invoice invoice){
        List<ReadTaxResponseDTO> listTax = new ArrayList<>();
        readInvoiceResponse.setListTax(listTax);
        for(Tax tax : invoice.getListTax()){
            var taxDTO = new ReadTaxResponseDTO(tax.getTaxId(), tax.getTaxName(), tax.getTaxPercentage());
            readInvoiceResponse.getListTax().add(taxDTO);
        }
    }

    UpdateInvoiceRequestDTO updateInvoiceToInvoiceDTO(Invoice invoice);
    Invoice updateInvoiceDTOToInvoice(UpdateInvoiceRequestDTO invoiceDTO);
}

