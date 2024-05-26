package com.megapro.invoicesync.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.megapro.invoicesync.dto.ProductMapper;
import com.megapro.invoicesync.dto.request.CreateProductRequestDTO;
import com.megapro.invoicesync.model.Product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExcelService {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InvoiceService invoiceService;

    public List<Product> processExcel(MultipartFile file) throws IOException {
    	log.info("Processing Excel file...");
        List<Map<String, Object>> excelDataList = parseExcel(file);
        System.out.println("LISSRR " + excelDataList.size());
        List<Product> listProduct = proceedToDatabase(excelDataList);
        if (listProduct.size() == 0){
            log.info("Excel data processed failed");
            return new ArrayList<>();
        }
        log.info("data saved into sql database");
        log.info("Excel file processed successfully.");
        return listProduct;
    }

    private List<Map<String, Object>> parseExcel(MultipartFile file) throws IOException {
        List<Map<String, Object>> excelDataList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            int columnCount = headerRow.getPhysicalNumberOfCells();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row currentRow = sheet.getRow(i);
                Map<String, Object> rowData = new HashMap<>();

                if (currentRow != null) {
                    for (int j = 0; j < columnCount; j++) {
                        Cell currentCell = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        String cellValue = getCellValueAsString(currentCell);
                        String headerValue = getCellValueAsString(headerRow.getCell(j));
                        rowData.put(headerValue, cellValue);
                    }
                }
                excelDataList.add(rowData);
            }
        }
        return excelDataList;
    }

    private String getCellValueAsString(Cell cell) {
        var cellType = cell.getCellType();
        if (cellType == 0){
            return String.valueOf(cell.getNumericCellValue());
        }
        else if (cellType == 1){
            return cell.getStringCellValue().trim();
        }
        return "";
    }

    private List<Product> proceedToDatabase(List<Map<String, Object>> excelDataList){
        List<Product> listProduct = new ArrayList<>();
        try {
            for (Map<String, Object> data : excelDataList){
                CreateProductRequestDTO productDTO = new CreateProductRequestDTO();
                productDTO.setDescription(data.get("description").toString());
                productDTO.setPrice(data.get("price").toString());
                productDTO.setQuantity(data.get("quantity").toString());
                productDTO.setTotalPrice(data.get("total price").toString());
                var product = productMapper.createProductRequestToProduct(productDTO);
                product.setInvoice(invoiceService.getDummyInvoice());
                if (product.getTotalPrice() == null){
                    var totalPrice = product.getPrice().doubleValue() * product.getQuantity().doubleValue();
                    BigDecimal fixedPrice = BigDecimal.valueOf(totalPrice);
                    product.setTotalPrice(fixedPrice);
                }
                productService.createProduct(product);
                listProduct.add(product);
            }
        }
        catch (NumberFormatException e){
            return new ArrayList<>();
        }
        return listProduct;
    }
}