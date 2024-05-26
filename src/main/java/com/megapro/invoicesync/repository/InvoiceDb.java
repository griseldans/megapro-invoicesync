package com.megapro.invoicesync.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.megapro.invoicesync.model.Invoice;
import jakarta.transaction.Transactional;


@Repository
@Transactional
public interface InvoiceDb extends JpaRepository<Invoice, UUID>{
   Invoice getInvoiceByInvoiceNumber(String id);
   @Query("SELECT i FROM Invoice i WHERE i.staffEmail LIKE 'dummy'")
   Invoice findDummyInvoice();
   List<Invoice> findByOrderByInvoiceNumberDesc();
   List<Invoice> findByStaffEmailIn(List<String> emails);
   List<Invoice> findByStaffEmailOrderByInvoiceNumberDesc(String email);
   List<Invoice> findByStatusOrderByInvoiceNumberDesc(String status);
   List<Invoice> findByStaffEmailAndStatusOrderByInvoiceNumberDesc(String email, String status);
   @Query("SELECT i FROM Invoice i JOIN Employee e ON i.staffEmail = e.email JOIN Role r ON e.role = r WHERE r.role LIKE %:roleName% AND i.status = :status")
   List<Invoice> findByEmployeeRoleNameAndStatus(String roleName, String status);
   Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

   // Dashboard finance director
   @Query("SELECT EXTRACT(MONTH FROM i.paymentDate) as month, SUM(i.grandTotal - i.taxTotal) as netRevenue " +
      "FROM Invoice i " +
      "WHERE i.status = 'Paid' AND EXTRACT(YEAR FROM i.paymentDate) = :year "  +
      "GROUP BY EXTRACT(MONTH FROM i.paymentDate) " +
      "ORDER BY month")
   List<Object[]> findMonthlyRevenue(@Param("year") int year);

   @Query("SELECT CASE WHEN i.status = 'Approved' THEN 'Unpaid' ELSE i.status END AS status, COUNT(i) " +
      "FROM Invoice i " +
      "WHERE i.status IN ('Paid', 'Approved') AND EXTRACT(YEAR FROM i.invoiceDate) = :year " +
      "GROUP BY CASE WHEN i.status = 'Approved' THEN 'Unpaid' ELSE i.status END")
   List<Object[]> findInvoiceCountsByStatus(@Param("year") int year);

   @Query("SELECT SUM(i.grandTotal) FROM Invoice i WHERE i.status = 'Paid' AND EXTRACT(YEAR FROM i.invoiceDate) = :year")
   BigDecimal findTotalPaidAmount(@Param("year") int year);

   @Query("SELECT SUM(i.grandTotal) FROM Invoice i WHERE i.status = 'Approved' AND EXTRACT(YEAR FROM i.invoiceDate) = :year")
   BigDecimal findTotalUnpaidAmount(@Param("year") int year);

   @Query("SELECT SUM(i.grandTotal) FROM Invoice i WHERE i.dueDate < :today AND i.status = 'Approved' AND EXTRACT(YEAR FROM i.invoiceDate) = :year")
   BigDecimal findTotalOverdueAmount(@Param("today") LocalDate today, @Param("year") int year);

   @Query("SELECT EXTRACT(MONTH FROM i.invoiceDate) AS month, COUNT(*) " +
         "FROM Invoice i " +
         "WHERE i.staffEmail <> 'dummy' AND EXTRACT(YEAR FROM i.invoiceDate) = :year " +
         "GROUP BY EXTRACT(MONTH FROM i.invoiceDate) " +
         "ORDER BY month")
   List<Object[]> findMonthlyInvoiceCounts(@Param("year") int year);

   @Query(value = "SELECT EXTRACT(MONTH FROM i.invoice_date) AS month, i.status, COUNT(i) " +
   "FROM Invoice i " +
   "WHERE i.status IN ('Paid', 'Approved') AND EXTRACT(YEAR FROM i.invoice_date) = :year " +
   "GROUP BY EXTRACT(MONTH FROM i.invoice_date), i.status " +
   "ORDER BY month", nativeQuery = true)
   List<Object[]> findMonthlyInvoiceStatusCounts(@Param("year") int year);

   @Query("SELECT EXTRACT(YEAR FROM i.approvedDate) AS year, EXTRACT(MONTH FROM i.approvedDate) AS month, COUNT(i) AS invoiceCount " +
      "FROM Invoice i " +
      "WHERE i.status IN ('Approved', 'Paid') AND EXTRACT(YEAR FROM i.approvedDate) = :year " +
      "GROUP BY EXTRACT(YEAR FROM i.approvedDate), EXTRACT(MONTH FROM i.approvedDate) " +
      "ORDER BY year, month")
   List<Object[]> findMonthlyInvoiceOutbound(@Param("year") int year);

   @Query("SELECT i.status AS status, COUNT(i) AS count " +
      "FROM Invoice i " +
      "WHERE i.staffEmail <> 'dummy' AND EXTRACT(YEAR FROM i.invoiceDate) = :year " + // Exclude records with 'dummy' email
      "GROUP BY i.status " +
      "ORDER BY i.status")
   List<Object[]> findInvoicesByStatus(@Param("year") int year);

   @Query("SELECT i.status AS status, " +
       "SUM(CASE WHEN i.status = 'Paid' THEN 1 ELSE 0 END) AS paidCount, " +
       "SUM(CASE WHEN i.status = 'Approved' THEN 1 ELSE 0 END) AS unpaidCount " +
       "FROM Invoice i " +
       "WHERE i.status IN ('Paid', 'Approved') AND EXTRACT(YEAR FROM i.invoiceDate) = :year " +
       "GROUP BY i.status " +
       "ORDER BY i.status")
   List<Object[]> findInvoiceCountsByPaidAndApproved(@Param("year") int year);

   @Query("SELECT EXTRACT(MONTH FROM i.paymentDate) AS month, SUM(i.taxTotal) AS totalTax " +
      "FROM Invoice i " +
      "WHERE i.status = 'Paid' AND EXTRACT(YEAR FROM i.paymentDate) = :year " +
      "GROUP BY EXTRACT(MONTH FROM i.paymentDate) " +
      "ORDER BY month")
   List<Object[]> findTotalTaxByMonth(@Param("year") int year);

   @Query(value = "SELECT * FROM Invoice i WHERE i.staff_email <> 'dummy' AND EXTRACT(YEAR FROM i.invoice_date) = :year ORDER BY i.invoice_date DESC LIMIT 5", nativeQuery = true)
   List<Invoice> findTopFiveNewestInvoices(@Param("year") int year);

   @Query(value = "SELECT * FROM Invoice i " +
           "WHERE i.status <> 'Paid' AND EXTRACT(YEAR FROM i.invoice_date) = :year " + // Exclude invoices with status 'Paid'
           "ORDER BY ABS(i.due_date - :today) ASC LIMIT 5", nativeQuery = true) // Order by closest due date
   List<Invoice> findTopFiveClosestDueDate(@Param("today") LocalDate today, @Param("year") int year);

   @Query(value = "SELECT * FROM Invoice i " +
               "WHERE i.staff_email = :staffEmail AND i.status = 'Approved' " +
               "AND EXTRACT(YEAR FROM i.approved_date) = :year " +
               "ORDER BY i.approved_date DESC LIMIT 5",
       nativeQuery = true)
   List<Invoice> findTop5ApprovedInvoicesByStaffEmail(@Param("staffEmail") String staffEmail, @Param("year") int year);

   @Query(value = "SELECT * FROM Invoice i " +
               "WHERE i.staff_email = :staffEmail AND i.status = 'Need Revision' " +
               "AND EXTRACT(YEAR FROM i.invoice_date) = :year " +
               "LIMIT 5",
       nativeQuery = true)
   List<Invoice> findTop5NeedRevisionInvoicesByStaffEmail(@Param("staffEmail") String staffEmail, @Param("year") int year);

   @Query(value = "SELECT * FROM Invoice i " + 
               "WHERE i.staff_email = :staffEmail AND EXTRACT(YEAR FROM i.invoice_date) = :year " + 
               "AND i.status <> 'Paid' " + 
               "ORDER BY ABS(i.due_date - :today) ASC LIMIT 5", 
       nativeQuery = true)
   List<Invoice> findTopFiveClosestDueDateByStaffEmail(@Param("today") LocalDate today, @Param("staffEmail") String staffEmail, @Param("year") int year);

   @Query("SELECT DISTINCT EXTRACT(YEAR FROM i.invoiceDate) FROM Invoice i ORDER BY EXTRACT(YEAR FROM i.invoiceDate)")
   List<Integer> findDistinctYears();

   @Query("SELECT COUNT(i) FROM Invoice i JOIN i.listApproval a " +
           "WHERE a.employee.email = :email AND i.status = 'Approved' " +
           "AND EXTRACT(YEAR FROM i.invoiceDate) = :year")
    int countApprovedInvoicesByApproverEmailAndYear(@Param("email") String email, @Param("year") int year);

    @Query("SELECT COUNT(i) FROM Invoice i JOIN i.listApproval a " +
           "WHERE a.employee.email = :email AND i.status = 'Need Approval' " +
           "AND EXTRACT(YEAR FROM i.invoiceDate) = :year")
    int countInvoicesWaitingApprovalByApproverEmail(@Param("email") String email, @Param("year") int year);
}  

