package com.megapro.invoicesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.Product;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface ProductDb extends JpaRepository<Product, UUID>{
    List<Product> findByInvoice(Invoice invoice);
    Product findByProductId(UUID productId);

    // Dashboard finance director
    @Query(value = "SELECT p.description, SUM(p.quantity) AS totalOrdered " +
           "FROM Product p JOIN Invoice i ON p.invoice_id = i.invoice_id "  +
            "WHERE EXTRACT(YEAR FROM i.invoice_date) = :year " +
           "GROUP BY p.description " +
           "ORDER BY totalOrdered DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTopProductsByQuantityOrdered(@Param("year") int year);
}
