package com.megapro.invoicesync.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="invoice_id")
    private UUID invoiceId;

    @Column(name="staff_email")
    private String staffEmail;

    @Column(name="invoice_number")
    private String invoiceNumber;

    @Column(name="invoice_date")
    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private LocalDate invoiceDate;

    @Column(name="due_date")
    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private LocalDate dueDate;

    @Column(name="total_words")
    private String totalWords;

    @Lob
    @Column(name="signature")
    private String signature;

    @Column(name="city")
    private String city;

    @Column(name="subtotal")
    private BigDecimal subtotal;

    // Ini discount (%)
    @Column(name="total_discount")
    private int totalDiscount;

    // Ini hasil perkalian discount dengan subtotal
    @Column(name="discount_total")
    private BigDecimal discountTotal;

    @ManyToMany
    @JoinTable(name = "invoice_tax", joinColumns = @JoinColumn(name = "invoice_id"),
            inverseJoinColumns = @JoinColumn(name = "tax_id"))
    List<Tax> listTax;

    @Column(name="total_tax")
    private BigDecimal taxTotal;

    @Column(name="grand_total")
    private BigDecimal grandTotal;

    @Column(name="account_number")
    private String accountNumber;

    @Column(name="bank_name")
    private String bankName;

    @Column(name="account_name")
    private String accountName;

    @OneToMany(mappedBy = "productId", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    private List<Product> listProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private Customer customer;

    @Column(name="status")
    private String status;

    @OneToMany(mappedBy = "invoice")
    @OrderBy("approval_id ASC")
    private List<Approval> listApproval;

    @OneToMany(mappedBy = "invoice")
    private List<FileModel> invoiceFiles;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "approved_date")
    private LocalDate approvedDate;
}
