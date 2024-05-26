package com.megapro.invoicesync.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification")
    private int notificationId;

    @Column(name="content")
    private String content;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "notification_employee")
    private Employee employee;

    @Column(name="invoice_id")
    private UUID invoiceId;

    @Column(name="invoice_number")
    private String invoiceNumber;

    @Column(name="approval_id", nullable = true)
    private int approvalId;

    @Column(name="is_read")
    private boolean isRead = false;

    @Column(name="notification_time")
    private LocalDateTime notificationTime;

    public Notification(String content, Employee employee, UUID invoiceId, String invoiceNumber) {
        this.content = content;
        this.employee = employee;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.notificationTime = LocalDateTime.now(ZoneId.systemDefault());
    }
}
