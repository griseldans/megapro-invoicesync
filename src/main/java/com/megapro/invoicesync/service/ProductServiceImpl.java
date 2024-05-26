package com.megapro.invoicesync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.megapro.invoicesync.dto.request.CreateProductRequestDTO;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.Product;
import com.megapro.invoicesync.repository.ProductDb;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@Transactional
public class ProductServiceImpl implements ProductService{
    @Autowired
    ProductDb productDb;

    @Override
    public void createProduct(Product product) {
        productDb.save(product);
    }
    
    @Override
    public List<Product> getAllProduct(){
        return productDb.findAll();
    }

    @Override
    public List<Product> getAllProductDummyInvoice(Invoice dummy) {
        List<Product> listProduct = getAllProduct();
        List<Product> listDummy = new ArrayList<>();
        for (Product p : listProduct){
            if (p.getInvoice().getInvoiceId().equals(dummy.getInvoiceId())){
                listDummy.add(p);
            }
        }
        return listDummy;
    }

    @Override
    public Product getProduct(CreateProductRequestDTO productDTO) {
        List<Product> listProduct = getAllProduct();
        for (Product p : listProduct){
            if (p.getDescription().equals(productDTO.getDescription())){
                return p;
            }
        }
        return null;
    }

    @Override
    public void delete(Product product) {
        product.setInvoice(null);
        productDb.delete(product);
    }

    @Override
    public Product getProductById(UUID id) {
        return productDb.findByProductId(id);
    }

    @Override
    public Product updateProduct(Product productDTO) {
        var product = getProductById(productDTO.getProductId());
        product.setQuantity(productDTO.getQuantity());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        double totalPrice = productDTO.getQuantity().doubleValue() * productDTO.getPrice().doubleValue();
        product.setTotalPrice(BigDecimal.valueOf(totalPrice));
        productDb.save(product);
        return product;
    }

    @Override
    public List<Product> getProductByInvoice(String id) {
        List<Product> listProduct = new ArrayList<>();
        for (Product p : getAllProduct()){
            if (p.getInvoice().getInvoiceId().toString().equals(id)){
                listProduct.add(p);
            }
        }
        return listProduct;
    }
}
