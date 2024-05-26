package com.megapro.invoicesync.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private int approvalId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "approval_employee")
    private Employee employee;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "approval_invoice")
    private Invoice invoice;

    @Column(name="rank")
    private int rank;

    @Column(name="cycle")
    private int cycle;

    @Column(name="shown")
    private boolean shown = false;

    @Column(name="approval_status")
    private String approvalStatus = "Pending Approval";

    @Column(name="comment")
    private String comment;

    @Column(name="assign_time")
    private LocalDateTime assignTime;

    // @Column(name="approval_time")
    // private LocalDate approvalTime;

    @Column(name="approval_time")
    private LocalDateTime approvalTime;

    @OneToMany(mappedBy = "approval")
    private List<FileModel> approvalFiles;
}
