package com.megapro.invoicesync.restcontroller;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.megapro.invoicesync.dto.response.InvoicePerMonthDTO;
import com.megapro.invoicesync.dto.response.InvoiceStatusCountDTO;
import com.megapro.invoicesync.dto.response.InvoiceStatusPaymentDTO;
import com.megapro.invoicesync.dto.response.MonthStatusDTO;
import com.megapro.invoicesync.dto.response.TopCustomerDTO;
import com.megapro.invoicesync.dto.response.TopProductDTO;
import com.megapro.invoicesync.dto.response.InvoicesStatusChartDTO;
import com.megapro.invoicesync.dto.response.MonthlyTaxDTO;
import com.megapro.invoicesync.dto.response.NewestInvoiceDTO;
import com.megapro.invoicesync.dto.response.OutboundInvoiceCountDTO;
import com.megapro.invoicesync.dto.response.RevenueDTO;
import com.megapro.invoicesync.service.DashboardService;


@RestController
public class DashboardRestController {

    @Autowired
    DashboardService dashboardService;
    
    // Dashboard finance director //

    @GetMapping("/api/dashboard/revenue")
    @ResponseBody
    public ResponseEntity<List<RevenueDTO>> getMonthlyRevenue(@RequestParam(required = false) Integer year) {
        List<RevenueDTO> revenues = dashboardService.getMonthlyRevenue(year);
        return new ResponseEntity<>(revenues, HttpStatus.OK);
    }
    
    @GetMapping("api/dashboard/top-customers")
    @ResponseBody
    public ResponseEntity<List<TopCustomerDTO>> showTopCustomers(@RequestParam(required = false) Integer year) {
        List<TopCustomerDTO> topCustomers = dashboardService.getTopCustomersByInvoiceCount(year);
        return new ResponseEntity<>(topCustomers, HttpStatus.OK);
    }

    @GetMapping("api/dashboard/top-products")
    @ResponseBody
    public ResponseEntity<List<TopProductDTO>> showTopProducts(@RequestParam(required = false) Integer year) {
        List<TopProductDTO> topProducts = dashboardService.getTopProductsByQuantityOrdered(year);
        return new ResponseEntity<>(topProducts, HttpStatus.OK);
    }

    @GetMapping("api/dashboard/invoice-ratio")
    @ResponseBody
    public ResponseEntity<List<InvoiceStatusCountDTO>> showInvoiceRatio(@RequestParam(required = false) Integer year) {
        List<InvoiceStatusCountDTO> invoiceStatusCounts = dashboardService.getInvoiceCountsByStatus(year);
        return new ResponseEntity<>(invoiceStatusCounts, HttpStatus.OK);
    }

    @GetMapping("api/dashboard/paid-amount")
    public ResponseEntity<BigDecimal> showPaidAmount(@RequestParam(required = false) Integer year) {
        var invoicePaidAmount = dashboardService.getInvoicePaidAmount(year);
        return new ResponseEntity<>(invoicePaidAmount, HttpStatus.OK);
    }

    @GetMapping("api/dashboard/unpaid-amount")
    public ResponseEntity<BigDecimal> showUnpaidAmount(@RequestParam(required = false) Integer year) {
        var invoiceUnpaidAmount = dashboardService.getInvoiceUnpaidAmount(year);
        return new ResponseEntity<>(invoiceUnpaidAmount, HttpStatus.OK);
    }

    @GetMapping("api/dashboard/overdue-amount")
    public ResponseEntity<BigDecimal> showOverdueAmount(@RequestParam(required = false) Integer year) {
        var invoiceOverdueAmount = dashboardService.getInvoiceOverdueAmount(year);
        return new ResponseEntity<>(invoiceOverdueAmount, HttpStatus.OK);
    }

    // Dashboard Manager Non Finance //

    @GetMapping("/api/dashboard/count-approved")
    public ResponseEntity<Integer> showCountApproved(@RequestParam(required = false) Integer year) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();  
        int count = dashboardService.totalInvoiceApproved(email, year);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/api/dashboard/waiting-approval")
    public ResponseEntity<Integer> showCountWaitingApproval(@RequestParam(required = false) Integer year) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();  
        int count = dashboardService.totalInvoiceWaitingApproved(email, year);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/api/dashboard/outbound-invoices") 
    @ResponseBody
    public ResponseEntity<List<OutboundInvoiceCountDTO>> showInvoiceOutboundAPI(@RequestParam(required = false) Integer year) {
        List<OutboundInvoiceCountDTO> response = dashboardService.getOutboundInvoicePerMonth(year);
        return new ResponseEntity<>(response, HttpStatus.OK); 
    }

    // Dashboard finance staff

    @GetMapping("/api/dashboard/invoice-status")
    @ResponseBody
    public ResponseEntity<List<InvoicesStatusChartDTO>> getInvoiceStatusData(@RequestParam(required = false) Integer year) {
        List<InvoicesStatusChartDTO> response = dashboardService.getInvoiceStatusCounts(year);
        return new ResponseEntity<>(response, HttpStatus.OK); // Return JSON data
    }

    @GetMapping("/api/dashboard/invoice-status-bar")
    @ResponseBody
    public ResponseEntity<List<InvoiceStatusPaymentDTO>> getInvoiceStatusCounts(@RequestParam(required = false) Integer year) {
        List<InvoiceStatusPaymentDTO> response = dashboardService.getInvoiceCountsByPaidAndApproved(year);
        return new ResponseEntity<>(response, HttpStatus.OK); // Return the JSON response
    }

    @GetMapping("/api/dashboard/tax-gain-chart")
    @ResponseBody
    public ResponseEntity<List<MonthlyTaxDTO>> getTaxGainData(@RequestParam(required = false) Integer year) {
        List<MonthlyTaxDTO> response = dashboardService.getMonthlyTaxGainFromPaidInvoices(year);
        return new ResponseEntity<>(response, HttpStatus.OK); 
    }

    @GetMapping("/api/dashboard/newest-five-invoices") // REST API endpoint
    @ResponseBody
    public ResponseEntity<List<NewestInvoiceDTO>> getFiveNewestInvoices(@RequestParam(required = false) Integer year) {
        List<NewestInvoiceDTO> response = dashboardService.getFiveNewestInvoices(year); // Fetch data
        return new ResponseEntity<>(response, HttpStatus.OK); // Return JSON data
    }

    @GetMapping("/api/dashboard/closest-due-five-invoices")
    public ResponseEntity<List<NewestInvoiceDTO>> getFiveDueClosestInvoices(@RequestParam(required = false) Integer year) {
        List<NewestInvoiceDTO> response = dashboardService.getFiveDueClosestInvoices(year); // Fetch sorted data
        return  new ResponseEntity<>(response, HttpStatus.OK); // Return JSON data
    }        
    
    // Dashboard manager finance //

    @GetMapping("/api/dashboard/invoices-per-month")
    @ResponseBody
    public ResponseEntity<List<InvoicePerMonthDTO>> getInvoicesPerMonth(@RequestParam(required = false) Integer year) {
        List<InvoicePerMonthDTO> invoicesPerMonth = dashboardService.getMonthlyInvoiceCounts(year);
        return new ResponseEntity<>(invoicesPerMonth, HttpStatus.OK);
    }

    @GetMapping("/api/invoices/status-per-month")
    public ResponseEntity<List<MonthStatusDTO>> getInvoiceStatusPerMonth(@RequestParam(required = false) Integer year) {
        List<MonthStatusDTO> results = dashboardService.getMonthlyInvoiceStatusCounts(year);
        return ResponseEntity.ok(results);
    }

    // Dashboard non-finance staff //

    @GetMapping("/api/dashboard/latest-approved")
    @ResponseBody
    public ResponseEntity<List<NewestInvoiceDTO>> getLatestApprovedInvoices(@RequestParam(required = false) Integer year) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        List<NewestInvoiceDTO> invoices = dashboardService.getTop5ApprovedInvoicesByStaffEmail(email, year);
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    @GetMapping("/api/dashboard/latest-need-revision")
    @ResponseBody
    public ResponseEntity<List<NewestInvoiceDTO>> getLatestNeedRevisionInvoices(@RequestParam(required = false) Integer year) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        List<NewestInvoiceDTO> invoices = dashboardService.getTop5NeedRevisionInvoicesByStaffEmail(email, year);
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    @GetMapping("/api/dashboard/due-by-email")
    @ResponseBody
    public ResponseEntity<List<NewestInvoiceDTO>> getFiveDueSoonInvoiceByEmail(@RequestParam(required = false) Integer year) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        List<NewestInvoiceDTO> invoices = dashboardService.getFiveDueClosestInvoicesByStaffEmail(email, year);
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    @GetMapping("/api/dashboard/years")
    @ResponseBody
    public ResponseEntity<List<Integer>> getInvoiceYears() {
        List<Integer> years = dashboardService.getDistinctInvoiceYears();
        return new ResponseEntity<>(years, HttpStatus.OK);
    }
}
