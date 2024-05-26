package com.megapro.invoicesync.restcontroller;

import org.springframework.web.bind.annotation.RestController;

import com.megapro.invoicesync.dto.ProductMapper;
import com.megapro.invoicesync.dto.request.CreateProductRequestDTO;
import com.megapro.invoicesync.dto.request.UpdateProductRequestDTO;
import com.megapro.invoicesync.dto.response.ReadProductResponseDTO;
import com.megapro.invoicesync.model.Product;
import com.megapro.invoicesync.service.ExcelService;
import com.megapro.invoicesync.service.InvoiceService;
import com.megapro.invoicesync.service.ProductService;
import com.megapro.invoicesync.service.TaxService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1")
public class ProductRestController {
    @Autowired
    ProductService productService;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    InvoiceService invoiceService;
    
    @Autowired
    TaxService taxService;

    @Autowired
    ExcelService excelService;

    // Create product when create invoice
    @PostMapping("/create-product")
    public ResponseEntity<ReadProductResponseDTO> createProduct(@RequestBody CreateProductRequestDTO productDTO) {
        var dummyInvoice = invoiceService.getDummyInvoice();
        var product = productMapper.createProductRequestToProduct(productDTO);
        product.setInvoice(dummyInvoice);
        double totalPrice = Double.parseDouble(productDTO.getTotalPrice());
        BigDecimal fixedPrice = BigDecimal.valueOf(totalPrice);
        product.setTotalPrice(fixedPrice);
        productService.createProduct(product);
        var readProductDTO = productMapper.readProductToProductDTO(product);
        return ResponseEntity.ok(readProductDTO);
    }

    // Create product by product document (.xlsx)
    @PostMapping("/create-product-document")
    public ResponseEntity<?> createProductByDocument(@RequestParam("productDocument") MultipartFile productDocument) throws IOException{
        if (productDocument != null && !productDocument.isEmpty()) {
            List<Product> listProduct = excelService.processExcel(productDocument);
            if (listProduct.size() != 0){
                List<ReadProductResponseDTO> listProductDTO = new ArrayList<>();
                for (Product p : listProduct) {
                    var productDTO = productMapper.readProductToProductDTO(p);
                    listProductDTO.add(productDTO);
                }
                return ResponseEntity.status(HttpStatus.OK).body(listProductDTO);
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No products found.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product document empty.");
        }
    }

    // Create product manually but the invoice already created (edit invoice)
    @PostMapping("/create-product/{invoiceId}")
    public ResponseEntity<ReadProductResponseDTO> updateProductInvoice(@RequestBody CreateProductRequestDTO productDTO,
                                                            @PathVariable("invoiceId") String invoiceId) {
        var invoice = invoiceService.getInvoiceById(UUID.fromString(invoiceId));
        var product = productMapper.createProductRequestToProduct(productDTO);
        product.setInvoice(invoice);
        double totalPrice = Double.parseDouble(productDTO.getTotalPrice());
        BigDecimal fixedPrice = BigDecimal.valueOf(totalPrice);
        product.setTotalPrice(fixedPrice);
        productService.createProduct(product);
        var readProductDTO = productMapper.readProductToProductDTO(product);
        return ResponseEntity.ok(readProductDTO);
    }

    // Create product by product document for edit invoice
    @PostMapping("/add-product/{invoiceId}")
    public ResponseEntity<?> addProductByDocument(@RequestParam("productDocument") MultipartFile productDocument,
                                                                            @PathVariable("invoiceId") String invoiceId) throws IOException{
        var invoice = invoiceService.getInvoiceById(UUID.fromString(invoiceId));
        if (productDocument != null && !productDocument.isEmpty()) {
            List<Product> listProduct = excelService.processExcel(productDocument);
            if (listProduct.size() != 0){
                List<ReadProductResponseDTO> listProductDTO = new ArrayList<>();
                for (Product p : listProduct) {
                    p.setInvoice(invoice);
                    var productDTO = productMapper.readProductToProductDTO(p);
                    listProductDTO.add(productDTO);
                }
                return ResponseEntity.status(HttpStatus.OK).body(listProductDTO);
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No products found.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product document empty.");
        }
    }


    @PostMapping("/product/{id}/delete")
    public ResponseEntity<Product> deleteProduct(@PathVariable("id") String productId){
        var product = productService.getProductById(UUID.fromString(productId));
        productService.delete(product);
        System.out.println("delete done");
        return ResponseEntity.ok(product);
    }

    @PostMapping("/update-update")
    public ResponseEntity<Product> updateProduct(@RequestBody UpdateProductRequestDTO productDTO){
        var productFromDTO = productMapper.updateProductRequestToProduct(productDTO);
        var product = productService.updateProduct(productFromDTO);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/invoice/product/{id}")
    public ResponseEntity<List<ReadProductResponseDTO>> getAllProduct(@PathVariable("id") String productId){
        System.out.println("Masuk ke rest controller product");
        List<Product> listProduct = productService.getProductByInvoice(productId);
        List<ReadProductResponseDTO> listProductDTO = new ArrayList<>();
        for (Product p : listProduct){
            var productDTO = productMapper.readProductToProductDTO(p);
            listProductDTO.add(productDTO);
        }
        return ResponseEntity.ok(listProductDTO);
    }
}
