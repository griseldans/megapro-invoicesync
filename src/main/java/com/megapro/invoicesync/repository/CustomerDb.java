package com.megapro.invoicesync.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.megapro.invoicesync.model.Customer;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface CustomerDb extends JpaRepository<Customer, UUID>{
    Customer findByCustomerId(UUID customerId);

    // Dashboard finance director
    @Query(value = "SELECT c.name, COUNT(i.invoice_id) AS invoiceCount " +
       "FROM customer c JOIN invoice i ON c.customer_id = i.customer_id " +
       "WHERE EXTRACT(YEAR FROM i.invoice_date) = :year " +
       "GROUP BY c.name " +
       "ORDER BY invoiceCount DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTopCustomersByInvoiceCount(@Param("year") int year);
}
