package com.megapro.invoicesync.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.megapro.invoicesync.dto.ApprovalMapper;
import com.megapro.invoicesync.dto.CustomerMapper;
import com.megapro.invoicesync.dto.FileMapper;
import com.megapro.invoicesync.dto.InvoiceMapper;
import com.megapro.invoicesync.dto.ProductMapper;
import com.megapro.invoicesync.dto.TaxMapper;
import com.megapro.invoicesync.dto.request.CreateCustomerRequestDTO;
import com.megapro.invoicesync.dto.request.CreateInvoiceRequestDTO;
import com.megapro.invoicesync.dto.request.UpdateInvoiceRequestDTO;
import com.megapro.invoicesync.repository.UserAppDb;
import com.megapro.invoicesync.service.ApprovalService;
import com.megapro.invoicesync.service.CustomerService;
import com.megapro.invoicesync.service.ExcelService;
import com.megapro.invoicesync.service.FilesStorageService;
import com.megapro.invoicesync.service.InvoiceService;
import com.megapro.invoicesync.service.NotificationService;
import com.megapro.invoicesync.service.TaxService;
import com.megapro.invoicesync.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.megapro.invoicesync.dto.response.ApproverDisplay;
import com.megapro.invoicesync.dto.response.NotificationResponseDTO;
import com.megapro.invoicesync.dto.response.ReadApprovalResponseDTO;
import com.megapro.invoicesync.dto.response.ReadFileResponseDTO;
import com.megapro.invoicesync.dto.response.ReadInvoiceResponse;
import com.megapro.invoicesync.dto.response.ReadTaxResponseDTO;
import com.megapro.invoicesync.model.Approval;
import com.megapro.invoicesync.model.Customer;
import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.FileModel;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.Product;
import com.megapro.invoicesync.model.Tax;
import com.megapro.invoicesync.model.UserApp;
import com.megapro.invoicesync.repository.InvoiceDb;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;



@Controller
public class InvoiceController {
    @Autowired
    InvoiceMapper invoiceMapper;

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    UserAppDb userAppDb;

    @Autowired
    InvoiceDb invoiceDb;

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    @Autowired
    TaxService taxService;

    @Autowired
    TaxMapper taxMapper;

    @Autowired
    UserService userService;

    @Autowired
    ExcelService excelService;

    @Autowired
    ApprovalService approvalService;

    @Autowired
    FilesStorageService fileService;

    @Autowired
    FileMapper fileMapper;

    @Autowired
    ApprovalMapper approvalMapper;

    @Autowired
    NotificationService notificationService;

    @GetMapping(value="/create-invoice")
    public String formCreateInvoice(Model model, @ModelAttribute("successMessage") String successMessage, 
                                    @ModelAttribute("errorMessage") String errorMessage){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        var invoiceDTO = new CreateInvoiceRequestDTO();
        invoiceDTO.setStaffEmail(email);
        invoiceDTO.setStatus("Draft");
        var customerDTO = new CreateCustomerRequestDTO();
        List<Customer> listCustomer = customerService.getAllCustomer();
        LocalDate date = invoiceDTO.getInvoiceDate();
        Employee employee = userService.findByEmail(email);
        var invoiceDummyId = invoiceService.getDummyInvoice().getInvoiceId();

        // Notification
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("dateInvoice", invoiceService.parseDate(invoiceDTO.getInvoiceDate()));
        model.addAttribute("date", String.format("%02d/%02d/%04d", date.getDayOfMonth(),  date.getMonth().getValue(), date.getYear()));
        model.addAttribute("status", invoiceDTO.getStatus());
        model.addAttribute("listCustomer", listCustomer);
        model.addAttribute("customerDTO", customerDTO);
        model.addAttribute("invoiceDTO", invoiceDTO);
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("employee", employee);
        model.addAttribute("invoiceDummyId", invoiceDummyId);
        return "invoice/form-create-invoice";
    }

    @PostMapping(value = "/create-invoice")
    public String createInvoice(@Valid CreateInvoiceRequestDTO invoiceDTO, Model model,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(value = "taxOption", required = false) List<Integer> selectedTaxIds,
                                @ModelAttribute("successMessage") String successMessage,
                                @ModelAttribute("errorMessage") String errorMessage,
                                @RequestParam(value = "base64String",required = false) MultipartFile imageDataUrl,
                                @RequestParam(value = "files", required = false) MultipartFile[] files) throws IOException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        try{
            byte[] imageBytes = imageDataUrl.getBytes();
            String bytesToString = invoiceService.translateByte(imageBytes);
            invoiceDTO.setSignature(bytesToString);

            var message = invoiceService.checkValidity(invoiceDTO, selectedTaxIds, email).split(",");
            var newInvoiceDTO = new CreateInvoiceRequestDTO();

            if((!files[0].getOriginalFilename().equals("")) && !(message[0].equals("errorMessage"))){
                fileService.save(files, UUID.fromString(message[2]));
            }

            model.addAttribute("email", email);
            model.addAttribute("role", role);
            model.addAttribute("invoiceDTO", newInvoiceDTO);
            model.addAttribute("successMessage", successMessage);
            model.addAttribute("errorMessage", errorMessage);
            
            redirectAttributes.addFlashAttribute(message[0], message[1]);
        } catch (IOException e){
            String mess = "Failed";
            redirectAttributes.addFlashAttribute("errorMessage", mess);
        }
        return "redirect:/create-invoice";
    }

    @GetMapping("/invoice/{invoiceNumber}")
    public String getDetailInvoice(@PathVariable("invoiceNumber") String encodedInvoiceNumber, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        String invoiceNumber = encodedInvoiceNumber.replace('_', '/');

        var invoice = invoiceService.getInvoiceByInvoiceNumber(invoiceNumber);
        List<Product> listProduct = invoiceService.getListProductInvoice(invoice);
        List<Tax> taxList = taxService.findAllTaxes();
        var invoiceDTO = invoiceMapper.readInvoiceToInvoiceResponse(invoice);
        List<UserApp> employees = invoiceService.getEligibleApproversForInvoice(invoice);
        model.addAttribute("employees", employees);
        List<Approval> approvers = approvalService.findApproversByInvoice(invoice);
        model.addAttribute("approvers", approvers);
        // Replace "ApproverRole" with the actual role name
        model.addAttribute("employees", employees);
        var date = invoiceDTO.getInvoiceDate();
        Employee employee = userService.findByEmail(email);

        var creatorInvoice = userService.findByEmail(invoice.getStaffEmail());
        model.addAttribute("creator", String.format("%s %s", creatorInvoice.getFirst_name(), creatorInvoice.getLast_name()));

        var emailPermission = email.equals(invoice.getStaffEmail());

        var files = fileService.findByFileInvoice(invoice);
        List<ReadFileResponseDTO> documents = new ArrayList<>();
        for (FileModel addedFile : files){
            if (addedFile != null){
                var addedFileDTO = fileMapper.fileModelToReadFileResponseDTO(addedFile);
                documents.add(addedFileDTO);
            }
        }

        List<ApproverDisplay> approverDisplays = invoiceService.getApproverDisplaysForInvoice(invoice);
        System.out.println(approverDisplays);
        model.addAttribute("approverDisplays", approverDisplays);
        model.addAttribute("documents", documents);
        model.addAttribute("image", invoice.getSignature());
        model.addAttribute("status", invoice.getStatus());
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("date", String.format("%02d/%02d/%04d", date.getDayOfMonth(), date.getMonth().getValue(), date.getYear()));
        model.addAttribute("taxList", taxList);
        model.addAttribute("invoice", invoiceDTO);
        model.addAttribute("dateInvoice", invoiceService.parseDate(invoiceDTO.getInvoiceDate()));
        model.addAttribute("employee", employee);
        model.addAttribute("emailPermission", emailPermission);

        // Bagian logs
        var approvals = invoice.getListApproval();
        List<ReadApprovalResponseDTO> approvalLogs = new ArrayList<>();
        for(Approval approval:approvals){
            if(approval.getApprovalStatus().equals("Need Approval")){
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

        // Notification
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        model.addAttribute("approvalLogs", approvalLogs);

        return "invoice/view-detail-invoice";
    }
    
    @GetMapping("/status/{status}")
    public String getInvoicesByStatus(@PathVariable String status, Model model) {
        List<Invoice> filteredInvoices = invoiceDb.findByStatusOrderByInvoiceNumberDesc(status);
        model.addAttribute("invoices", filteredInvoices);
        return ""; // isi halaman view all invoice
    }

    @GetMapping("/invoices")
    public String getAllInvoices(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserApp user = userAppDb.findByEmail(email); // Assuming userAppDb is a service/repository for UserApp entities
        String role = user.getRole().getRole(); // Fetch the role of the user
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        List<Invoice> invoiceList = invoiceService.retrieveAllInvoice();
        List<ReadInvoiceResponse> invoiceDTOList = new ArrayList<>();

        for (Invoice invoice : invoiceList) {
            ReadInvoiceResponse invoiceDTO = invoiceMapper.readInvoiceToInvoiceResponse(invoice);
            
            UserApp invoiceUser = userAppDb.findByEmail(invoice.getStaffEmail());
            
            // Cek apakah invoiceUser adalah null
            String staffRole = (invoiceUser != null) ? invoiceUser.getRole().getRole() : "Unknown Role";
        
            invoiceDTO.setStaffRole(staffRole);
            invoiceDTOList.add(invoiceDTO);
        }

        // Notification
        var employee = userService.findByEmail(email);
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        model.addAttribute("invoices", invoiceDTOList);
        return "invoice/viewall-invoices";
    }

    @GetMapping(value="/invoices", params = "status")
    public String getAllInvoices(@RequestParam(value = "status") String status, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserApp user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        model.addAttribute("status", status);

        List<Invoice> invoiceList = invoiceService.retrieveInvoicesByStatus(status);
        List<ReadInvoiceResponse> invoiceDTOList = new ArrayList<>();

        for (Invoice invoice : invoiceList) {
            ReadInvoiceResponse invoiceDTO = invoiceMapper.readInvoiceToInvoiceResponse(invoice);
            
            // Fetch the staff user for each invoice to get their role
            UserApp invoiceUser = userAppDb.findByEmail(invoice.getStaffEmail());

            String staffRole = (invoiceUser != null) ? invoiceUser.getRole().getRole() : "Unknown Role";

            // Set the staff role into the invoice DTO
            invoiceDTO.setStaffRole(staffRole);
            invoiceDTOList.add(invoiceDTO);
        }

        // Notification
        var employee = userService.findByEmail(email);
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        model.addAttribute("invoices", invoiceDTOList);
        return "invoice/viewall-invoices";
    }

    @GetMapping("/my-invoices")
    public String getMyInvoices(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Mendapatkan email pengguna yang sedang login
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        model.addAttribute("email", email);
        model.addAttribute("role", role);
        // Asumsi Anda memiliki metode di InvoiceService untuk mengambil invoice berdasarkan email staff
        List<Invoice> myInvoices = invoiceService.retrieveInvoicesByEmail(email);
        List<ReadInvoiceResponse> invoices = new ArrayList<>();

        for (Invoice invoice: myInvoices){
            var invoiceDTO = invoiceMapper.readInvoiceToInvoiceResponse(invoice);
            invoices.add(invoiceDTO);
        }

        // Notification
        var employee = userService.findByEmail(email);
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        model.addAttribute("invoices", invoices);
        return "invoice/my-invoices-view"; // Ganti dengan nama view Thymeleaf Anda
    }

    @GetMapping(value="/my-invoices", params = {"status"})
    public String getMyInvoices(@RequestParam(value = "status") String status, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Mendapatkan email pengguna yang sedang login
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        model.addAttribute("status", status);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        // Asumsi Anda memiliki metode di InvoiceService untuk mengambil invoice berdasarkan email staff
        List<Invoice> myInvoices = invoiceService.retrieveInvoicesByEmailAndStatus(email, status);
        
        // Mapping dari Invoice ke DTO jika diperlukan
        List<ReadInvoiceResponse> myInvoiceDTOs = myInvoices.stream()
                                                            .map(invoice -> invoiceMapper.readInvoiceToInvoiceResponse(invoice))
                                                            .collect(Collectors.toList());

        // Notification
        var employee = userService.findByEmail(email);
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        model.addAttribute("invoices", myInvoiceDTOs);
        return "invoice/my-invoices-view";
    }

    @GetMapping("/invoices/division/{division}")
    public String getInvoicesByDivision(@PathVariable("division") String requestedDivision, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Mendapatkan email pengguna yang sedang login
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        model.addAttribute("email", email);
        model.addAttribute("role", role);
        // Logika untuk mendapatkan role dan email pengguna yang terautentikasi sama
        
        // Tidak perlu memeriksa apakah role sesuai karena Anda sekarang bekerja berdasarkan divisi
        List<Invoice> invoiceList = invoiceService.retrieveInvoicesByDivision(requestedDivision);
        List<ReadInvoiceResponse> invoiceDTOList = invoiceList.stream()
                                                            .map(invoiceMapper::readInvoiceToInvoiceResponse)
                                                            .collect(Collectors.toList());

        model.addAttribute("invoices", invoiceDTOList);
        model.addAttribute("division", requestedDivision); // Ganti role dengan division

        // Notification
        var employee = userService.findByEmail(email);
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));
        
        return "invoice/viewall-invoices-division";
    }

    @GetMapping(value="/invoices/division/{division}", params = "status")
    public String getInvoicesByDivision(
            @PathVariable("division") String requestedDivision, 
            @RequestParam(value = "status") String status,
            Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Mendapatkan email pengguna yang sedang login
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("status", status);

        List<Invoice> invoiceList = invoiceService.retrieveInvoicesByDivisionAndStatus(requestedDivision, status);
        List<ReadInvoiceResponse> invoiceDTOList = invoiceList.stream()
                                                            .map(invoiceMapper::readInvoiceToInvoiceResponse)
                                                            .collect(Collectors.toList());

        model.addAttribute("invoices", invoiceDTOList);
        model.addAttribute("division", requestedDivision);

        // Notification
        var employee = userService.findByEmail(email);
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));
        
        return "invoice/viewall-invoices-division";
    }

    @GetMapping("/invoice/{invoiceNumber}/edit")
    public String formEditInvoice(@PathVariable("invoiceNumber") String encodedInvoiceNumber,
                                    Model model, HttpServletRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        String invoiceNumber = encodedInvoiceNumber.replace('_', '/');

        var invoice = invoiceService.getInvoiceByInvoiceNumber(invoiceNumber);
        UpdateInvoiceRequestDTO invoiceDTO = invoiceMapper.updateInvoiceToInvoiceDTO(invoice);
        List<Product> listProduct = invoiceService.getListProductInvoice(invoice);
        var date = invoice.getInvoiceDate();
        Employee employee = userService.findByEmail(email);

        for(ReadTaxResponseDTO taxDTO:invoiceDTO.getListTax()){
            System.out.printf("Tax id: %d", taxDTO.getTaxId());
        }

        model.addAttribute("listProduct", listProduct);
        model.addAttribute("role", role);
        model.addAttribute("date", String.format("%02d/%02d/%04d", date.getDayOfMonth(),  date.getMonth().getValue(), date.getYear()));
        model.addAttribute("invoiceNumber", invoiceNumber);
        model.addAttribute("dateInvoice", invoiceService.parseDate(invoice.getInvoiceDate()));
        model.addAttribute("customer", invoice.getCustomer());
        model.addAttribute("email", email);
        model.addAttribute("invoiceDTO", invoiceDTO);
        model.addAttribute("currentSignature", invoice.getSignature());
        model.addAttribute("employee", employee);

        List<Tax> taxLists = taxService.findAllTaxes();
        var taxList = taxMapper.taxListToReadTaxResponseDTOList(taxLists);
        model.addAttribute("taxList", taxList);

        // Notification
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        return "invoice/form-edit-invoice";
    }
    
    @PostMapping("/invoice/edit")
    public String editInvoice(@ModelAttribute UpdateInvoiceRequestDTO invoiceDTO, Model model,
                            @RequestParam(value = "taxOption", required = false) List<Integer> selectedTaxIds,
                            @RequestParam(value = "base64String",required = false ) MultipartFile imageDataUrl,
                            RedirectAttributes redirectAttributes) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();

        if (imageDataUrl.getSize() != 0){
            byte[] imageBytes = imageDataUrl.getBytes();
            String bytesToString = invoiceService.translateByte(imageBytes);
            invoiceDTO.setSignature(bytesToString);
        }

        // var check = invoiceService.checkValidityUpdate(invoiceDTO);
        // var message = check.split(",");

        // if (message[0].equals("errorMessage")){
        //     redirectAttributes.addFlashAttribute(message[0], message[1]);
        // } else{
        var invoiceFromDTO = invoiceMapper.updateInvoiceDTOToInvoice(invoiceDTO);
        var invoice = invoiceService.updateInvoice(invoiceFromDTO, selectedTaxIds);
        // redirectAttributes.addFlashAttribute(message[0], message[1]); 
        // }

        String encodedInvoiceNumber = invoice.getInvoiceNumber().replace("/", "_");
        // model.addAttribute("successMessage", successMessage);
        // model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("image", invoice.getSignature());
        return String.format("redirect:/invoice/%s", encodedInvoiceNumber);
    }

    @PostMapping("/invoice/{invoiceNumber}/add-approver")
    public String addApprover(@PathVariable("invoiceNumber") String invoiceNumber,
                              @RequestParam Map<String, String> allParams,
                              RedirectAttributes redirectAttributes) {
        try {
            String decodedInvoiceNumber = invoiceNumber.replace('_', '/');
            Invoice invoice = invoiceService.getInvoiceByInvoiceNumber(decodedInvoiceNumber);
    
            allParams.forEach((key, value) -> {
                if (key.startsWith("approverEmail") && !value.isEmpty()) {
                    invoiceService.addApproverToInvoice(invoice.getInvoiceId(), value);
                }
            });

            invoice.setStatus("Pending Approval");
            invoiceDb.save(invoice);

            // Generate Notifications
            var firstApproval = invoice.getListApproval().get(0);
            var firstApprover = firstApproval.getEmployee();
            notificationService.generateInvoiceApproverNotification(firstApprover.getEmail(), invoice.getInvoiceId(), firstApproval.getApprovalId());
    
            redirectAttributes.addFlashAttribute("success", "Approvers successfully added.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            String simplifiedMessage = e.getMessage().replaceAll("java\\.lang\\.(IllegalArgumentException|IllegalStateException): ", "");
            redirectAttributes.addFlashAttribute("error", simplifiedMessage);
        } catch (Exception e) {
            // Catch all other exceptions that you might not have foreseen.
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again.");
        }
        return "redirect:/invoice/" + invoiceNumber.replace('/', '_');
    }

    @PostMapping("/invoice/{invoiceNumber}/reassign-approver")
    public String reAddApprover(@PathVariable("invoiceNumber") String invoiceNumber,
                              @RequestParam Map<String, String> allParams,
                              RedirectAttributes redirectAttributes) {
        
        String decodedInvoiceNumber = invoiceNumber.replace('_', '/');
        Invoice invoice = invoiceService.getInvoiceByInvoiceNumber(decodedInvoiceNumber);

        List<Approval> existingApprovals = new ArrayList<>(invoice.getListApproval());
        for(Approval existApproval : existingApprovals){
            var approvalId = existApproval.getApprovalId();
            approvalService.resetApproval(approvalId);
        }

        var oldSize = invoice.getListApproval().size();
        allParams.forEach((key, value) -> {
            if (key.startsWith("approverEmail") && !value.isEmpty()) {
                var approval = invoiceService.readdApproverToInvoice(invoice.getInvoiceId(), value, oldSize);
                approval.setRank(invoice.getListApproval().size()-oldSize);
                System.out.printf("Size baru segini %d size lama segini %d\n", invoice.getListApproval().size(), oldSize);
                if(approval.getRank()==1){
                    approval.setShown(true);
                }
            }
        });

        invoice.setStatus("Pending Approval");
        invoiceDb.save(invoice);

        // Generate notification
        var firstApproval = invoice.getListApproval().get(0);
        var firstApprover = firstApproval.getEmployee();
        notificationService.generateInvoiceApproverNotification(firstApprover.getEmail(), invoice.getInvoiceId(), firstApproval.getApprovalId());

        redirectAttributes.addFlashAttribute("success", "Approvers successfully added.");
        return "redirect:/invoice/" + invoiceNumber.replace('/', '_');
    }

    @GetMapping("/invoice/{invoiceNumber}/mark-as-paid")
    public String statusPaid(@PathVariable("invoiceNumber") String encodedInvoiceNumber, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userAppDb.findByEmail(email);
        String role = user.getRole().getRole();
        String invoiceNumber = encodedInvoiceNumber.replace('_', '/');

        var invoice = invoiceService.getInvoiceByInvoiceNumber(invoiceNumber);
        invoice.setStatus("Paid");
        invoice.setPaymentDate(LocalDate.now());

        List<Product> listProduct = invoiceService.getListProductInvoice(invoice);
        List<Tax> taxList = taxService.findAllTaxes();
        var invoiceDTO = invoiceMapper.readInvoiceToInvoiceResponse(invoice);
        List<String> rolesToFind = Arrays.asList("Finance Manager", "Finance Director", "Finance Staff");
        List<UserApp> employees = userAppDb.findByRoleNames(rolesToFind);
        List<Approval> approvers = approvalService.findApproversByInvoice(invoice);
        model.addAttribute("approvers", approvers);
        // Replace "ApproverRole" with the actual role name
        model.addAttribute("employees", employees);
        var date = invoiceDTO.getInvoiceDate();
        Employee employee = userService.findByEmail(email);

        var emailPermission = email.equals(invoice.getStaffEmail());

        var files = fileService.findByFileInvoice(invoice);
        List<ReadFileResponseDTO> documents = new ArrayList<>();
        for (FileModel addedFile : files){
            if (addedFile != null){
                var addedFileDTO = fileMapper.fileModelToReadFileResponseDTO(addedFile);
                documents.add(addedFileDTO);
            }
        }
        
        var creatorInvoice = userService.findByEmail(invoice.getStaffEmail());
        model.addAttribute("creator", String.format("%s %s", creatorInvoice.getFirst_name(), creatorInvoice.getLast_name()));
        model.addAttribute("documents", documents);
        model.addAttribute("image", invoice.getSignature());
        model.addAttribute("status", invoice.getStatus());
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("date", String.format("%02d/%02d/%04d", date.getDayOfMonth(),  date.getMonth().getValue(), date.getYear()));
        model.addAttribute("taxList", taxList);
        model.addAttribute("invoice", invoiceDTO);
        model.addAttribute("dateInvoice", invoiceService.parseDate(invoiceDTO.getInvoiceDate()));
        model.addAttribute("employee", employee);
        List<ApproverDisplay> approverDisplays = invoiceService.getApproverDisplaysForInvoice(invoice);
        model.addAttribute("approverDisplays", approverDisplays);
        model.addAttribute("emailPermission", emailPermission);

        // Bagian logs
        var approvals = invoice.getListApproval();
        List<ReadApprovalResponseDTO> approvalLogs = new ArrayList<>();
        for(Approval approval:approvals){
            if(approval.getApprovalStatus().equals("Need Approval")){
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

        model.addAttribute("approvalLogs", approvalLogs);

        // Notification
        var notifications = notificationService.getEmployeeNotification(employee);
        model.addAttribute("notifications0", notifications.get(0));
        model.addAttribute("notifications1", notifications.get(1));
        model.addAttribute("notifications7", notifications.get(2));
        model.addAttribute("notifications30", notifications.get(3));

        return "invoice/view-detail-invoice";

    }
    
}
