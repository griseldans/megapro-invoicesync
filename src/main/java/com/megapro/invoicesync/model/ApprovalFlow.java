package com.megapro.invoicesync.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "approval_flow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int approvalRank;
    private String approverRole;
    private Long nominalRange;
}