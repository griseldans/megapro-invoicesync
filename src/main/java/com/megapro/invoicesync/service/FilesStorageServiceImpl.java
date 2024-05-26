package com.megapro.invoicesync.service;

import java.util.UUID;
import java.util.stream.Stream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.megapro.invoicesync.dto.FileMapper;
import com.megapro.invoicesync.model.FileModel;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.repository.FileDb;

@Service
public class FilesStorageServiceImpl implements FilesStorageService {

    @Autowired
    ApprovalService approvalService;

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    FileDb fileDb;

    @Autowired
    FileMapper fileMapper;

    @Override
    public void save(MultipartFile[] files, int approvalId) {
        var approval = approvalService.findApprovalByApprovalId(approvalId);

        for(MultipartFile file:files){
            FileModel fileModel = fileMapper.multipartfileToFileModel(file);
            fileModel.setApproval(approval);
            fileDb.save(fileModel);
        }
    }

    @Override
    public void save(MultipartFile[] files, UUID id) {
        var invoice = invoiceService.getInvoiceById(id);

        for(MultipartFile file:files){
            FileModel fileModel = fileMapper.multipartfileToFileModel(file);
            fileModel.setInvoice(invoice);
            fileDb.save(fileModel);
        }
    }

    @Override
    public FileModel getFile(UUID id) {
        return fileDb.findFileModelByFileId(id);
    }

    @Override
    public Stream<FileModel> findAll() {
        return fileDb.findAll().stream();
    }

    @Override 
    public List<FileModel> findByFileInvoice(Invoice invoice){
        return fileDb.findByInvoice(invoice);
    }
    
}