package com.megapro.invoicesync.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.megapro.invoicesync.dto.response.InvoicePerMonthDTO;
import com.megapro.invoicesync.dto.response.InvoiceStatusCountDTO;
import com.megapro.invoicesync.dto.response.InvoiceStatusPaymentDTO;
import com.megapro.invoicesync.dto.response.InvoicesStatusChartDTO;
import com.megapro.invoicesync.dto.response.MonthStatusDTO;
import com.megapro.invoicesync.dto.response.MonthlyTaxDTO;
import com.megapro.invoicesync.dto.response.NewestInvoiceDTO;
import com.megapro.invoicesync.dto.response.OutboundInvoiceCountDTO;
import com.megapro.invoicesync.dto.response.RevenueDTO;
import com.megapro.invoicesync.dto.response.TopCustomerDTO;
import com.megapro.invoicesync.dto.response.TopProductDTO;
import com.megapro.invoicesync.model.Approval;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.repository.CustomerDb;
import com.megapro.invoicesync.repository.EmployeeDb;
import com.megapro.invoicesync.repository.InvoiceDb;
import com.megapro.invoicesync.repository.ProductDb;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Autowired
    InvoiceDb invoiceDb;

    @Autowired
    CustomerDb customerDb;

    @Autowired
    ProductDb productDb;

    @Autowired
    EmployeeDb employeeDb;
    
    @Override
    public List<RevenueDTO> getMonthlyRevenue(Integer year) {
        List<Object[]> revenueData = invoiceDb.findMonthlyRevenue(year);
        BigDecimal[] monthlyRevenue = new BigDecimal[12]; 
        Arrays.fill(monthlyRevenue, BigDecimal.ZERO); 

        for (Object[] data : revenueData) {
            int monthIndex = (int) data[0] - 1;
            monthlyRevenue[monthIndex] = (BigDecimal) data[1];
        }

        List<RevenueDTO> revenues = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String month = Month.of(i + 1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            revenues.add(new RevenueDTO(month, monthlyRevenue[i]));
        }
        return revenues;
    }

    @Override
    public List<InvoiceStatusCountDTO> getInvoiceCountsByStatus(Integer year) {
        List<Object[]> invoiceStatusCountsData = invoiceDb.findInvoiceCountsByStatus(year);
        List<InvoiceStatusCountDTO> invoiceStatusCounts = new ArrayList<>();

        for (Object[] data : invoiceStatusCountsData) {
            String status = (String) data[0];
            Long invoiceCount = (Long) data[1];
            invoiceStatusCounts.add(new InvoiceStatusCountDTO(status, invoiceCount));
        }
        return invoiceStatusCounts;
    }

    @Override
    public List<TopCustomerDTO> getTopCustomersByInvoiceCount(Integer year) {
        List<Object[]> topCustomersData = customerDb.findTopCustomersByInvoiceCount(year);
        List<TopCustomerDTO> topCustomers = new ArrayList<>();
        for (Object[] data : topCustomersData) {
            String customerName = (String) data[0];
            Long invoiceCount = (Long) data[1];
            topCustomers.add(new TopCustomerDTO(customerName, invoiceCount));
        }
        return topCustomers;
    }

    @Override
    public List<TopProductDTO> getTopProductsByQuantityOrdered(Integer year) {
        List<Object[]> topProductsData = productDb.findTopProductsByQuantityOrdered(year);
        List<TopProductDTO> topProducts = new ArrayList<>();

        for (Object[] data : topProductsData) {
            String productName = (String) data[0];
            BigDecimal invoiceCount = (BigDecimal) data[1];
            topProducts.add(new TopProductDTO(productName, invoiceCount));
        }
        return topProducts;
    }

    @Override
    public BigDecimal getInvoicePaidAmount(Integer year) {
        // var invoicePaidAmount = dashboardService.getInvoicePaidAmount();
        return invoiceDb.findTotalPaidAmount(year);
    }

    @Override
    public BigDecimal getInvoiceUnpaidAmount(Integer year) {
        return invoiceDb.findTotalUnpaidAmount(year);
    }

    @Override
    public BigDecimal getInvoiceOverdueAmount(Integer year) {
        return invoiceDb.findTotalOverdueAmount(LocalDate.now(), year);
    }

    @Override
    public List<InvoicePerMonthDTO> getMonthlyInvoiceCounts(Integer year) {
        List<Object[]> invoiceData = invoiceDb.findMonthlyInvoiceCounts(year);
        List<InvoicePerMonthDTO> invoicesPerMonth = new ArrayList<>();

        for (Month month : Month.values()) {
            invoicesPerMonth.add(new InvoicePerMonthDTO(month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), 0));
        }
        for (Object[] data : invoiceData) {
            int monthIndex = ((Number) data[0]).intValue() - 1; 
            int count = ((Number) data[1]).intValue();
            invoicesPerMonth.get(monthIndex).setCount(count);
        }
        return invoicesPerMonth;
    }

    @Override
    public List<MonthStatusDTO> getMonthlyInvoiceStatusCounts(Integer year) {
        List<Object[]> results = invoiceDb.findMonthlyInvoiceStatusCounts(year);
        Map<Integer, MonthStatusDTO> monthDataMap = new HashMap<>();
        for (Month month : Month.values()) {
            String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthDataMap.put(month.getValue(), new MonthStatusDTO(monthName, 0, 0));
        }

        for (Object[] result : results) {
            int month = ((Number) result[0]).intValue();
            String status = (String) result[1];
            int count = ((Number) result[2]).intValue();

            MonthStatusDTO dto = monthDataMap.get(month);
            if ("Paid".equals(status)) {
                dto.setPaidInvoices(count);
            } else if ("Approved".equals(status)) {
                dto.setUnpaidInvoices(count);
            }
        }
        return new ArrayList<>(monthDataMap.values());
    }

    @Override
    public List<OutboundInvoiceCountDTO> getOutboundInvoicePerMonth(Integer year){
        List<Object[]> rawData = invoiceDb.findMonthlyInvoiceOutbound(year);
        List<OutboundInvoiceCountDTO> response = new ArrayList<>();

        // Convert raw data to DTO
        final String[] monthNames = {
            "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
        };

        for (Object[] item : rawData) {
            int yearGraph = ((Number) item[0]).intValue();
            int month = ((Number) item[1]).intValue();
            int count = ((Number) item[2]).intValue();

            String monthName = monthNames[month - 1] + " " + yearGraph;
            response.add(new OutboundInvoiceCountDTO(monthName, count));
        }

        return response;
    }

    @Override
    public int totalInvoiceApproved(String email, Integer year) {
        var employee = employeeDb.findByEmail(email);
        var approvalList = employee.getListApproval();
        int count = 0;

        for(Approval approval : approvalList){
            if(approval.isShown() && approval.getApprovalStatus().equals("Approved")){
                count++;
            }
        }

        return count;

    } 

    @Override
    public int totalInvoiceWaitingApproved(String email, Integer year) {
        var employee = employeeDb.findByEmail(email);
        var approvalList = employee.getListApproval();
        int count = 0;

        for(Approval approval : approvalList){
            if(approval.isShown() && approval.getApprovalStatus().equals("Need Approval")){
                count++;
            }
        }

        return count;
    }

    @Override 
    public List<InvoicesStatusChartDTO> getInvoiceStatusCounts(Integer year){
        List<Object[]> rawData = invoiceDb.findInvoicesByStatus(year);
        List<InvoicesStatusChartDTO> response = new ArrayList<>();

        // Convert raw data to DTOs
        for (Object[] item : rawData) {
            String status = (String) item[0]; // Status of the invoice
            int count = ((Number) item[1]).intValue(); // Count of invoices with this status

            response.add(new InvoicesStatusChartDTO(status, count));
        }
        return response;
    }

    @Override
    public List<InvoiceStatusPaymentDTO> getInvoiceCountsByPaidAndApproved(Integer year) {
        List<Object[]> rawData = invoiceDb.findInvoiceCountsByPaidAndApproved(year);
        List<InvoiceStatusPaymentDTO> response = new ArrayList<>();

        for (Object[] item : rawData) {
            String status = (String) item[0]; // Invoice status
            int paidCount = ((Number) item[1]).intValue();
            int unpaidCount = ((Number) item[2]).intValue();
            response.add(new InvoiceStatusPaymentDTO(status, paidCount, unpaidCount));
        }

        return response;
    }

    @Override
    public List<MonthlyTaxDTO> getMonthlyTaxGainFromPaidInvoices(Integer year){
        List<Object[]> rawResults = invoiceDb.findTotalTaxByMonth(year);
        Map<Integer, Double> monthToTax = new HashMap<>();

        // Fill the map with query results
        for (Object[] result : rawResults) {
            int month = (int) result[0]; // Extracted month
            double totalTax = ((Number) result[1]).doubleValue(); // Total tax

            monthToTax.put(month, totalTax);
        }

        // Complete list of months with zero as default value
        List<Object[]> fullResults = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            fullResults.add(new Object[]{
                i, // Month (as integer)
                monthToTax.getOrDefault(i, 0.0) // Default to zero if no data
            });
        }

        List<MonthlyTaxDTO> response = new ArrayList<>();

        // Convert raw data to DTOs
        for (Object[] item : fullResults) {
            int month = ((Number) item[0]).intValue();
            double totalTax = ((Number) item[1]).doubleValue();
            response.add(new MonthlyTaxDTO(month, totalTax));
        }

        return response;
    }

    @Override
    public List<NewestInvoiceDTO> getFiveNewestInvoices(Integer year) {
        List<Invoice> rawData = invoiceDb.findTopFiveNewestInvoices(year);
        List<NewestInvoiceDTO> response = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Invoice invoice : rawData) {
            response.add(new NewestInvoiceDTO(
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate().format(formatter),
                invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Unknown",
                invoice.getGrandTotal().doubleValue()
            ));
        }

        return response;
    }
    
    @Override
    public List<NewestInvoiceDTO> getFiveDueClosestInvoices(Integer year){
        LocalDate today = LocalDate.now(); // Get today's date
        List<Invoice> rawData = invoiceDb.findTopFiveClosestDueDate(today, year); // Fetch all ordered by closest due date

        List<NewestInvoiceDTO> response = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Format for dates

        for (int i = 0; i < Math.min(5, rawData.size()); i++) { // Get top 5 closest due dates
            Invoice invoice = rawData.get(i);
            response.add(new NewestInvoiceDTO(
                invoice.getInvoiceNumber(),
                invoice.getDueDate().format(formatter), // Format date
                invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Unknown", // Handle null customers
                invoice.getGrandTotal().doubleValue() // Convert BigDecimal to double
            ));
        }
        return response;
    }

    @Override
    public List<NewestInvoiceDTO> getTop5ApprovedInvoicesByStaffEmail(String staffEmail, Integer year) {
        List<Invoice> invoices = invoiceDb.findTop5ApprovedInvoicesByStaffEmail(staffEmail, year);
        
        List<NewestInvoiceDTO> invoicesDTO = invoices.stream()
        .map(invoice -> new NewestInvoiceDTO(
            invoice.getInvoiceNumber(),
            invoice.getApprovedDate().toString(),
            invoice.getCustomer().getName(),
            invoice.getGrandTotal().doubleValue()))
        .collect(Collectors.toList());

        return invoicesDTO;
    }

    @Override
    public List<NewestInvoiceDTO> getTop5NeedRevisionInvoicesByStaffEmail(String staffEmail, Integer year) {
        List<Invoice> invoices = invoiceDb.findTop5NeedRevisionInvoicesByStaffEmail(staffEmail,year);
        List<NewestInvoiceDTO> invoicesDTO = invoices.stream()
        .map(invoice -> new NewestInvoiceDTO(
            invoice.getInvoiceNumber(),
            invoice.getInvoiceDate().toString(),
            invoice.getCustomer().getName(),
            invoice.getGrandTotal().doubleValue()))
        .collect(Collectors.toList());

        return invoicesDTO;
    }

    @Override
    public List<NewestInvoiceDTO> getFiveDueClosestInvoicesByStaffEmail(String staffEmail, Integer year) {
        LocalDate today = LocalDate.now();
        List<Invoice> invoices = invoiceDb.findTopFiveClosestDueDateByStaffEmail(today, staffEmail, year);
        List<NewestInvoiceDTO> invoicesDTO = invoices.stream()
        .map(invoice -> new NewestInvoiceDTO(
            invoice.getInvoiceNumber(),
            invoice.getDueDate().toString(),
            invoice.getCustomer().getName(),
            invoice.getGrandTotal().doubleValue()))
        .collect(Collectors.toList());

        return invoicesDTO;
    }

    @Override
    public List<Integer> getDistinctInvoiceYears() {
        return invoiceDb.findDistinctYears();
    }
}
