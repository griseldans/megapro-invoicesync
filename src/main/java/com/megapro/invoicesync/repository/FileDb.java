package com.megapro.invoicesync.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megapro.invoicesync.model.FileModel;
import com.megapro.invoicesync.model.Invoice;

import jakarta.transaction.Transactional;
import java.util.List;


@Repository
@Transactional
public interface FileDb extends JpaRepository<FileModel,UUID> {
    FileModel findFileModelByFileId(UUID fileId);
    List<FileModel> findByInvoice(Invoice invoice);
}
