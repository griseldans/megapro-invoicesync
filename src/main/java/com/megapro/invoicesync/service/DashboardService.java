package com.megapro.invoicesync.service;

import java.util.List;

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

import java.math.BigDecimal;
public interface DashboardService {
    List<RevenueDTO> getMonthlyRevenue(Integer year);
    List<InvoiceStatusCountDTO> getInvoiceCountsByStatus(Integer year);
    List<TopCustomerDTO> getTopCustomersByInvoiceCount(Integer year);
    List<TopProductDTO> getTopProductsByQuantityOrdered(Integer year);
    List<OutboundInvoiceCountDTO> getOutboundInvoicePerMonth(Integer year);
    int totalInvoiceApproved(String email, Integer year);
    int totalInvoiceWaitingApproved(String email, Integer year);
    List<InvoicesStatusChartDTO> getInvoiceStatusCounts(Integer year);
    List<InvoiceStatusPaymentDTO> getInvoiceCountsByPaidAndApproved(Integer year);
    List<MonthlyTaxDTO> getMonthlyTaxGainFromPaidInvoices(Integer year);
    List<NewestInvoiceDTO> getFiveNewestInvoices(Integer year);
    List<NewestInvoiceDTO> getFiveDueClosestInvoices(Integer year);
    BigDecimal getInvoicePaidAmount(Integer year);
    BigDecimal getInvoiceUnpaidAmount(Integer year);
    BigDecimal getInvoiceOverdueAmount(Integer year);
    List<InvoicePerMonthDTO> getMonthlyInvoiceCounts(Integer year);
    List<MonthStatusDTO> getMonthlyInvoiceStatusCounts(Integer year);
    List<NewestInvoiceDTO> getTop5ApprovedInvoicesByStaffEmail(String staffEmail, Integer year);
    List<NewestInvoiceDTO> getTop5NeedRevisionInvoicesByStaffEmail(String staffEmail, Integer year);
    List<NewestInvoiceDTO> getFiveDueClosestInvoicesByStaffEmail(String staffEmail, Integer year);
    List<Integer> getDistinctInvoiceYears();

}
