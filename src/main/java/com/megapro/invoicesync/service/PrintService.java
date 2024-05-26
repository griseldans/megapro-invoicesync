package com.megapro.invoicesync.service;

import java.util.List;
import java.util.ArrayList;
import com.megapro.invoicesync.dto.FileMapper;
import com.megapro.invoicesync.dto.response.ReadFileResponseDTO;
import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.FileModel;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.Product;

import org.springframework.stereotype.Service;
import java.io.IOException;

import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PrintService {
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserService userService;

    @Autowired
    private FilesStorageService fileService;

    @Autowired
    private FileMapper fileMapper;

    public byte[] generateInvoice(Invoice invoice) throws IOException {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        Context context = new Context();
        String invoiceDate = invoiceService.parseDate(invoice.getInvoiceDate());
        String dueDate = invoiceService.parseDate(invoice.getDueDate());
        Employee employee = userService.findByEmail(invoice.getStaffEmail());
        String employeeName = String.format("%s %s", employee.getFirst_name(), employee.getLast_name());
        String grandTotal = currencyFormat.format(invoice.getGrandTotal());
        String subtotal = currencyFormat.format(invoice.getSubtotal());
        String taxes = currencyFormat.format(invoice.getTaxTotal());
        List<Product> listProduct = invoiceService.getListProductInvoice(invoice);
        var files = fileService.findByFileInvoice(invoice);
        List<ReadFileResponseDTO> documents = new ArrayList<>();
        for (FileModel addedFile : files){
            if (addedFile != null){
                var addedFileDTO = fileMapper.fileModelToReadFileResponseDTO(addedFile);
                documents.add(addedFileDTO);
            }
        }
        context.setVariable("invoiceNumber", invoice.getInvoiceNumber());
        context.setVariable("customerName", invoice.getCustomer().getName());
        context.setVariable("customerAddress", invoice.getCustomer().getAddress());
        context.setVariable("customerPhone", invoice.getCustomer().getPhone());
        context.setVariable("customerContact", invoice.getCustomer().getContact());
        context.setVariable("dueDate", dueDate);
        context.setVariable("invoiceDate", invoiceDate);
        context.setVariable("subtotal", subtotal);
        context.setVariable("taxes", taxes);
        context.setVariable("grandTotal", grandTotal);
        context.setVariable("totalDiscount", String.valueOf(invoice.getTotalDiscount()));
        context.setVariable("status", invoice.getStatus());
        context.setVariable("totalWords", invoice.getTotalWords());
        context.setVariable("accountNumber", invoice.getAccountNumber());
        context.setVariable("accountName", invoice.getAccountName());
        context.setVariable("bankName", invoice.getBankName());
        context.setVariable("city", invoice.getCity());
        context.setVariable("signature", "data:image/jpeg;base64,"+invoice.getSignature());
        context.setVariable("employeeName", employeeName);
        context.setVariable("listProduct", listProduct);
        context.setVariable("documents", documents);

        String processedHtml = templateEngine.process("data/template", context);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        HtmlConverter.convertToPdf(processedHtml, stream);

        byte[] bytes = stream.toByteArray();
        stream.flush();

        return bytes;
    }
}