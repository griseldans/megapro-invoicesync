package com.megapro.invoicesync.service;

import java.util.stream.Stream;
import java.util.UUID;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.megapro.invoicesync.model.FileModel;
import com.megapro.invoicesync.model.Invoice;

public interface FilesStorageService {
    public void save(MultipartFile[] files, int id);
    public void save(MultipartFile[] files, UUID id);
    public FileModel getFile(UUID id);
    public Stream<FileModel> findAll();
    public List<FileModel> findByFileInvoice(Invoice invoice);
}
