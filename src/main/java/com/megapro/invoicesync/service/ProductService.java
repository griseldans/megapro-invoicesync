package com.megapro.invoicesync.service;

import com.megapro.invoicesync.dto.request.CreateProductRequestDTO;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.Product;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    void createProduct(Product product);
    List<Product> getAllProduct();
    List<Product> getAllProductDummyInvoice(Invoice dummy);
    Product getProduct(CreateProductRequestDTO productDTO);
    void delete(Product product);
    Product getProductById(UUID id);
    Product updateProduct(Product productDTO);
    List<Product> getProductByInvoice(String id);
}
