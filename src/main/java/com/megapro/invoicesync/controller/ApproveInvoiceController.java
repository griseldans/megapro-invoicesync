package com.megapro.invoicesync.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.megapro.invoicesync.dto.ApprovalMapper;
import com.megapro.invoicesync.dto.FileMapper;
import com.megapro.invoicesync.dto.InvoiceMapper;
import com.megapro.invoicesync.dto.request.UpdateApprovalRequestDTO;
import com.megapro.invoicesync.dto.response.ReadApprovalResponseDTO;
import com.megapro.invoicesync.dto.response.ReadFileResponseDTO;
import com.megapro.invoicesync.dto.response.ReadInvoiceResponse;
import com.megapro.invoicesync.model.Approval;
import com.megapro.invoicesync.model.FileModel;
import com.megapro.invoicesync.model.Product;
import com.megapro.invoicesync.model.Tax;
import com.megapro.invoicesync.repository.EmployeeDb;
import com.megapro.invoicesync.repository.UserAppDb;
import com.megapro.invoicesync.service.ApprovalService;
import com.megapro.invoicesync.service.FilesStorageService;
import com.megapro.invoicesync.service.InvoiceService;
import com.megapro.invoicesync.service.NotificationService;
import com.megapro.invoicesync.service.TaxService;
import com.megapro.invoicesync.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ApproveInvoiceController {

    @Autowired
    UserAppDb userAppDb;

    @Autowired
    EmployeeDb employeeDb;

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    InvoiceMapper invoiceMapper;

    @Autowired
    ApprovalMapper approvalMapper;

    @Autowired
    ApprovalService approvalService;

    @Autowired
    TaxService taxService;

    @Autowired
    FilesStorageService fileService;

    @Autowired
    FileMapper fileMapper;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserService userService;

    @GetMapping("/approval")
    public String approveInvoicePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var employee = employeeDb.findByEmail(email);
        String role = employee.getRole().getRole();
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        // Content
        var invoiceList = approvalService.getEmployeeApprovalInvoice(employee);
        var needApproval = invoiceList.get(0);
        var previousApproval = invoiceList.get(1);
        model.addAttribute("needApproval", needApproval);
        model.addAttribute("previousApproval", previousApproval);

        // Notification
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        return "approve-invoice/list-approval.html";
    }
    
    // Detail invoice untuk diapprove
    @GetMapping("/approval/{invoiceNumber}")
    public String getApprovalDetail(
                            @PathVariable("invoiceNumber") String encodedInvoiceNumber, 
                            @RequestParam("approvalId") int approvalId,
                            Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        String invoiceNumber = encodedInvoiceNumber.replace('_', '/');

        // Bagian detail invoice
        var invoice = invoiceService.getInvoiceByInvoiceNumber(invoiceNumber);
        List<Product> listProduct = invoiceService.getListProductInvoice(invoice);
        List<Tax> taxList = taxService.findAllTaxes();
        var invoiceDTO = invoiceMapper.readInvoiceToInvoiceResponse(invoice);
        var date = invoiceDTO.getInvoiceDate();
        var approvalStatus = approvalService.findApprovalByApprovalId(approvalId).getApprovalStatus();

        model.addAttribute("image", invoice.getSignature());
        model.addAttribute("status", approvalStatus);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("date", String.format("%02d/%02d/%04d", date.getDayOfMonth(),  date.getMonth().getValue(), date.getYear()));
        model.addAttribute("taxList", taxList);
        model.addAttribute("invoice", invoiceDTO);
        model.addAttribute("dateInvoice", invoiceService.parseDate(invoiceDTO.getInvoiceDate()));

        // Bagian logs
        var approvals = invoice.getListApproval();
        List<ReadApprovalResponseDTO> approvalLogs = new ArrayList<>();
        var approvalDTO = new UpdateApprovalRequestDTO();
        for(Approval approval:approvals){
            if(approval.getApprovalStatus().equals("Need Approval")){
                approvalDTO = approvalMapper.approvalToUpdateApprovalRequestDTO(approval);
                break;
            }
            var filesLog = approval.getApprovalFiles();
            List<ReadFileResponseDTO> filesDTO = new ArrayList<>();
            if(filesLog != null || filesLog.size()!=0){
                for(FileModel fileModel : filesLog){
                    var fileDTO = fileMapper.fileModelToReadFileResponseDTO(fileModel);
                    filesDTO.add(fileDTO);
                }
            }
            var approvalLog = approvalMapper.approvalToReadApprovalResponseDTO(approval);
            approvalLog.setFilesDTO(filesDTO);
            approvalLogs.add(approvalLog);
        }
        
        var files = fileService.findByFileInvoice(invoice);
        List<ReadFileResponseDTO> documents = new ArrayList<>();
        for (FileModel addedFile : files){
            if (addedFile != null){
                var addedFileDTO = fileMapper.fileModelToReadFileResponseDTO(addedFile);
                documents.add(addedFileDTO);
            }
        }

        var employee = userService.findByEmail(email);

        model.addAttribute("documents", documents);
        model.addAttribute("approvalDTO", approvalDTO);
        model.addAttribute("approvalLogs", approvalLogs);
        model.addAttribute("employee", employee);

        // Notification
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        return "approve-invoice/approval-page.html";
    }

    @PostMapping("/approve-invoice")
    public String approve(UpdateApprovalRequestDTO updateApprovalRequestDTO, RedirectAttributes redirectAttributes) {
        
        var approval = approvalService.findApprovalByApprovalId(updateApprovalRequestDTO.getApprovalId());
        approval.setApprovalStatus("Approved");
        approval.setApprovalTime(LocalDateTime.now());
        
        
        var invoice = approval.getInvoice();
        var approvalList = invoice.getListApproval();
        boolean isLastApproval = approval.equals(approvalList.get(approvalList.size() - 1));

        if (isLastApproval) {
            invoice.setStatus("Approved");
            invoice.setApprovedDate(LocalDate.now());
            invoiceService.save(invoice);

            // Notification
            var invoiceMaker = invoice.getStaffEmail();
            notificationService.generateInvoiceMakerNotification(invoiceMaker, invoice.getInvoiceId());
        } else {
            int currentApprovalIndex = approvalList.indexOf(approval);
            var nextApproval = approvalList.get(currentApprovalIndex + 1);
            nextApproval.setShown(true);
            nextApproval.setAssignTime(LocalDateTime.now());

            // Notification
            var approverEmail = approval.getEmployee().getEmail();
            notificationService.generateInvoiceApproverNotification(approverEmail,invoice.getInvoiceId(), nextApproval.getApprovalId());
        }

        approvalService.saveApproval(approval);
        invoiceService.save(invoice);
        
        var invoiceNumber = approval.getInvoice().getInvoiceNumber().replace('/', '_');
        redirectAttributes.addFlashAttribute("message", "Invoice Approved Successfully");

        return "redirect:/approval/" + invoiceNumber + "?approvalId=" + approval.getApprovalId();
    }

    @PostMapping("/revision-invoice")
    public String reviseInvoice(UpdateApprovalRequestDTO updateApprovalDTO,
                                @RequestParam("files") MultipartFile[] files) {
        var approval = approvalService.findApprovalByApprovalId(updateApprovalDTO.getApprovalId());
        approval.setApprovalStatus("Need Revision");
        approval.setComment(updateApprovalDTO.getComment());
        approval.setApprovalTime((LocalDateTime.now()));

        fileService.save(files, updateApprovalDTO.getApprovalId());

        approvalService.saveApproval(approval);

        var invoice = approval.getInvoice();
        invoice.setStatus("Need Revision");
        invoiceService.save(invoice);

        // Notification
        var invoiceMaker = invoice.getStaffEmail();
        notificationService.generateInvoiceMakerNotification(invoiceMaker, invoice.getInvoiceId());

        var invoiceNumber = approval.getInvoice().getInvoiceNumber().replace('/', '_');
        
        return "redirect:/approval/"+invoiceNumber+"?approvalId="+updateApprovalDTO.getApprovalId();
    }
    
    @PostMapping("/reject-invoice")
    public String rejectInvoice(UpdateApprovalRequestDTO updateApprovalDTO) {
        var approval = approvalService.findApprovalByApprovalId(updateApprovalDTO.getApprovalId());
        approval.setApprovalStatus("Rejected");
        approval.setApprovalTime((LocalDateTime.now()));
        approvalService.saveApproval(approval);

        var invoice = approval.getInvoice();
        invoice.setStatus("Rejected");
        invoiceService.save(invoice);

        // Notification
        var invoiceMaker = invoice.getStaffEmail();
        notificationService.generateInvoiceMakerNotification(invoiceMaker, invoice.getInvoiceId());

        var invoiceNumber = approval.getInvoice().getInvoiceNumber().replace('/', '_');
        
        return "redirect:/approval/"+invoiceNumber+"?approvalId="+updateApprovalDTO.getApprovalId();
    }

    @GetMapping("/approve-invoice/download-files/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
        FileModel fileModel = fileService.getFile(fileId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileModel.getFileName() + "\"")
            .body(fileModel.getFileData());
    }

}
