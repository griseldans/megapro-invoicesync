package com.megapro.invoicesync.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "tax")
public class Tax {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tax_id")
    private int taxId;

    @NotNull
    @Column(name = "tax_name")
    private String taxName;

    @NotNull
    @Column(name = "tax_percentage")
    private int taxPercentage;

    @ManyToMany(mappedBy = "listTax", fetch = FetchType.LAZY)
    List<Invoice> listInvoice;
}
