package com.megapro.invoicesync.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.megapro.invoicesync.dto.request.CreateProductRequestDTO;
import com.megapro.invoicesync.dto.request.UpdateProductRequestDTO;
import com.megapro.invoicesync.dto.response.ReadProductResponseDTO;
import com.megapro.invoicesync.model.Product;

@Mapper(componentModel="spring")
public interface ProductMapper {
    @Mapping(target="totalPrice", ignore=true)
    Product createProductRequestToProduct(CreateProductRequestDTO productDTO);

    Product updateProductRequestToProduct(UpdateProductRequestDTO productDTO);

    ReadProductResponseDTO readProductToProductDTO(Product product);
}
