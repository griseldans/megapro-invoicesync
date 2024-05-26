package com.megapro.invoicesync.model;

import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "files")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class FileModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "file_id")
    private UUID fileId;

    @Column(name = "file_name")
    @NotNull
    private String fileName;

    @Column(name = "file_type")
    @NotNull
    private String fileType;

    @Lob
    @Column(name = "file_data")
    @NotNull
    private byte[] fileData;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_invoice")
    private Invoice invoice;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_approval")
    private Approval approval;
}
