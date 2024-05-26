package com.megapro.invoicesync.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID productId;

    @Column(name="description", nullable=false)
    private String description;

    @Column(name="quantity")
    private BigDecimal quantity;

    @Column(name="price")
    private BigDecimal price;

    @Column(name="total_price")
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}

