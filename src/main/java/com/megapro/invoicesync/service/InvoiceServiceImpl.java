package com.megapro.invoicesync.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.megapro.invoicesync.dto.InvoiceMapper;
import com.megapro.invoicesync.dto.request.CreateInvoiceRequestDTO;
import com.megapro.invoicesync.dto.request.UpdateInvoiceRequestDTO;
import com.megapro.invoicesync.dto.response.ApproverDisplay;
import com.megapro.invoicesync.model.Approval;
import com.megapro.invoicesync.model.ApprovalFlow;
import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.Invoice;
import com.megapro.invoicesync.model.Product;
import com.megapro.invoicesync.model.Tax;
import com.megapro.invoicesync.model.UserApp;
import com.megapro.invoicesync.repository.ApprovalDb;
import com.megapro.invoicesync.repository.ApprovalFlowDb;
import com.megapro.invoicesync.repository.EmployeeDb;
import com.megapro.invoicesync.repository.InvoiceDb;
import com.megapro.invoicesync.repository.ProductDb;
import com.megapro.invoicesync.repository.TaxDb;
import com.megapro.invoicesync.repository.UserAppDb;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService{
    @Autowired
    InvoiceDb invoiceDb;

    @Autowired
    ProductService productService;

    @Autowired
    ProductDb productDb;

    @Autowired
    TaxDb taxDb;

    @Autowired
    UserAppDb userAppDb;

    @Autowired
    EmployeeDb employeeDb;

    @Autowired
    ApprovalDb approvalDb;

    @Autowired
    CustomerService customerService;

    @Autowired
    InvoiceMapper invoiceMapper;

    @Autowired
    ApprovalFlowDb approvalFlowDb;

    @Override
    public void createInvoice(Invoice invoice, String email) {
        invoice.setStaffEmail(email);
        invoiceDb.save(invoice);
    }

    @Override
    public long countInvoice(){
        return invoiceDb.count();
    }

    @Override
    public void attributeInvoice(Invoice invoice, List<Integer> listTax) {
        long count = countInvoice();
        String countStr = String.format("%04d", count);
        var invoiceDate = invoice.getInvoiceDate();
        int month = invoiceDate.getMonthValue();
        String monthInRoman = convertToRoman(month);
        String year = String.valueOf(invoiceDate.getYear());
        String invoiceNumber = String.format("INV-%s/Krida/%s/%s",countStr,monthInRoman,year);
        invoice.setInvoiceNumber(invoiceNumber);
        var dummy = getDummyInvoice();
        List<Product> newListProduct = new ArrayList<>();
        List<Product> listProduct = productService.getAllProductDummyInvoice(dummy);
        for (Product p : listProduct) {
            p.setInvoice(invoice);
            newListProduct.add(p);
        }
        invoice.setListProduct(newListProduct);
        invoice.setListTax(taxDb.findByTaxIdIn(listTax));
        calculateSubtotal(invoice);
        calculateDiscount(invoice);
        calculateTax(invoice);
        calculateGrandTotal(invoice);
    }

    @Override
    public String translateByte(byte[] byteFile){
        return Base64.getEncoder().encodeToString(byteFile);
    }

    public static String convertToRoman(int num) {
        int[] VALUES = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
        String[] NUMERALS = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };

        StringBuilder romanNumeral = new StringBuilder();
        int i = 0;
        while (num > 0) {
            if (num - VALUES[i] >= 0) {
                romanNumeral.append(NUMERALS[i]);
                num -= VALUES[i];
            } else {
                i++;
            }
        }
        return romanNumeral.toString();
    }

    private void calculateSubtotal(Invoice invoice){
        double total = 0;
        var listInvoice = getListProductInvoice(invoice);
        for (Product p : listInvoice){
            total += p.getTotalPrice().doubleValue();
        }
        invoice.setSubtotal(BigDecimal.valueOf(total));
    }

    private void calculateDiscount(Invoice invoice){
        double subtotal = invoice.getSubtotal().doubleValue();
        double discount = (invoice.getTotalDiscount()/100.0)*subtotal;
        double afterDiscount = subtotal-discount;
        invoice.setGrandTotal(BigDecimal.valueOf(afterDiscount));
        invoice.setDiscountTotal(BigDecimal.valueOf(discount));
    }

    private void calculateTax(Invoice invoice){
        double total = 0;
        for(Tax tax:invoice.getListTax()){
            total += (tax.getTaxPercentage()*invoice.getGrandTotal().doubleValue()/100.0);
        }
        invoice.setTaxTotal(BigDecimal.valueOf(total));
    }

    private void calculateGrandTotal(Invoice invoice){
        BigDecimal total = invoice.getGrandTotal().add(invoice.getTaxTotal());
        invoice.setGrandTotal(total);
    }

    @Override
    public List<Invoice> retrieveAllInvoice() {
        return invoiceDb.findByOrderByInvoiceNumberDesc();
    }

    @Override
    public Invoice getInvoiceById(UUID id){
        for (Invoice inv : retrieveAllInvoice()) {
            if (inv.getInvoiceId().equals(id)) {
                return inv;
            }
        }
        return null;
    }

    @Override
    public List<Invoice> getInvoiceByStaffEmail(String email) {
        return invoiceDb.findByStaffEmailOrderByInvoiceNumberDesc(email);
    }

    @Override
    public List<Product> getListProductInvoice(Invoice invoice) {
        List<Product> listProduct = productService.getAllProduct();
        List<Product> listProductInInvoice = new ArrayList<>();
        for (Product p: listProduct){
            if (p.getInvoice().getInvoiceId().equals(invoice.getInvoiceId())){
                listProductInInvoice.add(p);
            }
        }
        return listProductInInvoice;
    }

    @Override
    public List<Invoice> retrieveInvoicesByRole(String role) {
        List<UserApp> usersInRole = userAppDb.findByRoleName(role);
        List<String> emailsInRole = usersInRole.stream()
                                               .map(UserApp::getEmail)
                                               .collect(Collectors.toList());
        return invoiceDb.findByStaffEmailIn(emailsInRole);
    }

    @Override
    public List<Invoice> retrieveInvoicesByEmail(String email) {
        // Asumsi Anda memiliki metode di InvoiceRepository untuk mengambil invoice berdasarkan email staff
        return invoiceDb.findByStaffEmailOrderByInvoiceNumberDesc(email);
    }

    @Override
    public List<Invoice> retrieveInvoicesByDivision(String division) {
        // Ambil semua user dari divisi tertentu
        List<UserApp> usersInDivision = userAppDb.findAll().stream()
                                                  .filter(user -> user.getRole().getRole().contains(division))
                                                  .collect(Collectors.toList());
        
        // Ambil email dari semua user ini
        List<String> emailsInDivision = usersInDivision.stream()
                                                        .map(UserApp::getEmail)
                                                        .collect(Collectors.toList());
        
        // Gunakan email ini untuk menemukan semua invoice yang terkait
        return invoiceDb.findByStaffEmailIn(emailsInDivision);
    }
    
    
    
    public List<Invoice> retrieveInvoicesByEmailAndStatus(String email, String status) {
        if(status == null || status.equals("")){
            return invoiceDb.findByStaffEmailOrderByInvoiceNumberDesc(email);
        }
        return invoiceDb.findByStaffEmailAndStatusOrderByInvoiceNumberDesc(email, status);
    }

    @Override
    public List<Invoice> retrieveInvoicesByDivisionAndStatus(String division, String status) {
        return invoiceDb.findByEmployeeRoleNameAndStatus(division, status);
    }

    @Override
    public List<Invoice> retrieveInvoicesByStatus(String status) {
        if(status == null || status == ""){
            return invoiceDb.findAll();
        } else {

            return invoiceDb.findByStatusOrderByInvoiceNumberDesc(status);
        }
    }

    @Override
    public Invoice getDummyInvoice() {
        return invoiceDb.findDummyInvoice();
    }

    @Override
    public Invoice getInvoiceByInvoiceNumber(String invoiceNumber) {
        Optional<Invoice> invoice = invoiceDb.findByInvoiceNumber(invoiceNumber);
        return invoice.orElseThrow(() -> new EntityNotFoundException("Invoice with number: " + invoiceNumber + " was not found."));
    }
    
    @Override
    public String parseDate(LocalDate localDate){
        int day = localDate.getDayOfMonth();
        int monthIndex = localDate.getMonthValue();
        int year = localDate.getYear();
        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                               "July", "August", "September", "October", "November", "December"};
        String formattedDate = day + " " + monthNames[monthIndex-1] + " " + year;
        return formattedDate;
    }



@Override
public void addApproverToInvoice(UUID invoiceId, String approverEmail) {
    Invoice invoice = invoiceDb.findById(invoiceId)
        .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));

    Employee employee = employeeDb.findByEmail(approverEmail);

    if (approvalDb.existsByInvoiceAndEmployee(invoice, employee)) {
        throw new IllegalStateException("Employee already added as an approver for this invoice.");
    }

    List<ApprovalFlow> applicableFlows = approvalFlowDb.findAllByOrderByNominalRangeAsc().stream()
        .filter(flow -> invoice.getGrandTotal().compareTo(BigDecimal.valueOf(flow.getNominalRange())) >= 0)
        .collect(Collectors.toList());

    if (applicableFlows.isEmpty()) {
        throw new IllegalArgumentException("No approval flow matches the invoice's nominal range.");
    }

    List<Approval> existingApprovals = approvalDb.findByInvoice(invoice);
    if (existingApprovals.size() >= applicableFlows.size()) {
        throw new IllegalStateException("The number of approvers for this invoice has reached the limit based on its total.");
    }

    // Determine the next required role from the approval flow
    if (existingApprovals.size() < applicableFlows.size()) {
        String requiredRole = applicableFlows.get(existingApprovals.size()).getApproverRole();
        if (!employee.getRole().getRole().equals(requiredRole)) {
            throw new IllegalArgumentException("The approver must have the role " + requiredRole + " for this rank.");
        }
    }

    Approval approval = new Approval();
    approval.setEmployee(employee);
    approval.setInvoice(invoice);
    approval.setApprovalStatus("Need Approval");
    approval.setApprovalTime(LocalDateTime.now());
    approval.setRank(existingApprovals.size() + 1); // Set the next rank based on existing approvals
    approval.setShown(approval.getRank() == 1); // Show only the first rank initially

    approvalDb.save(approval);
}

@Override
public Approval readdApproverToInvoice(UUID invoiceId, String approverEmail, int oldSize) {
    Invoice invoice = invoiceDb.findById(invoiceId)
        .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));

    Employee employee = employeeDb.findByEmail(approverEmail);

    List<ApprovalFlow> applicableFlows = approvalFlowDb.findAllByOrderByNominalRangeAsc().stream()
        .filter(flow -> invoice.getGrandTotal().compareTo(BigDecimal.valueOf(flow.getNominalRange())) >= 0)
        .collect(Collectors.toList());

    if (applicableFlows.isEmpty()) {
        throw new IllegalArgumentException("No approval flow matches the invoice's nominal range.");
    }

    List<Approval> existingApprovals = approvalDb.findByInvoice(invoice);

    Approval approval = new Approval();
    approval.setEmployee(employee);
    approval.setInvoice(invoice);
    approval.setApprovalStatus("Need Approval");
    approval.setApprovalTime(LocalDateTime.now());
    // approval.setRank(existingApprovals.size() + 1);
    // approval.setShown(approval.getRank() == 1);

    approvalDb.save(approval);
    invoice.getListApproval().add(approval);
    return approval;
}

@Override
public List<UserApp> getEligibleApproversForInvoice(Invoice invoice) {
    BigDecimal total = invoice.getGrandTotal();
    List<ApprovalFlow> applicableFlows = approvalFlowDb.findAllByOrderByNominalRangeAsc().stream()
        .filter(flow -> total.compareTo(BigDecimal.valueOf(flow.getNominalRange())) >= 0)
        .collect(Collectors.toList());

    Set<String> eligibleRoles = applicableFlows.stream()
        .map(ApprovalFlow::getApproverRole)
        .collect(Collectors.toSet());

    return userAppDb.findByRoleNames(new ArrayList<>(eligibleRoles));
}

    
    public String checkValidity(CreateInvoiceRequestDTO invoiceDTO, List<Integer> selectedTaxIds, String email) {
        var res = "";
        if (invoiceDTO.getCustomerId() == null){
            res = "errorMessage, Customer can't be empty";
        }
        else if (invoiceDTO.getAccountName() == null || invoiceDTO.getBankName() == null){
            res = "errorMessage, Please give the correct input for payment information";
        }
        else if (invoiceDTO.getAccountNumber() != null && !isNumeric(invoiceDTO.getAccountNumber())){
            res = "errorMessage, Please give the correct input for payment information";
        }
        else if (invoiceDTO.getDueDate() == null){
            res = "errorMessage, Please select invoice due date";
        }
        else if (invoiceDTO.getCity().isBlank() || isNumeric(invoiceDTO.getCity())){
            res = "errorMessage, Please fill the city name";
        }
        else if (invoiceDTO.getTotalWords().isBlank()){
            res = "errorMessage, Please fill the amount in words";
        }
        else if (invoiceDTO.getSignature().isEmpty()){
            res = "errorMessage, Please upload your signature image";
        }
        else {
            // TODO: perlu logic kalau grand total lebih kecil dari nominal flow pertama
            invoiceDTO.setStatus("Waiting for Approver");
            res = "successMessage, Invoice successfully created";
        }    

        var mes = res.split(",");
        if (mes[0].equals("errorMessage")){
            List<Product> listProduct = productService.getAllProductDummyInvoice(getDummyInvoice());
            for (Product p : listProduct){
                productService.delete(p);
            }
            return res;
        }
        var customer = customerService.getCustomerById(invoiceDTO.getCustomerId());
        var invoice = invoiceMapper.createInvoiceRequestToInvoice(invoiceDTO);
        invoice.setCustomer(customer);
        attributeInvoice(invoice, selectedTaxIds);
        createInvoice(invoice, email);
        res += ","+invoice.getInvoiceId();
        return res;
    }

    // ... other methods in InvoiceServiceImpl ...
    @Override
    public List<ApproverDisplay> getApproverDisplaysForInvoice(Invoice invoice) {
        BigDecimal total = invoice.getGrandTotal();
        List<ApprovalFlow> applicableFlows = approvalFlowDb.findAllByOrderByNominalRangeAsc().stream()
            .filter(flow -> total.compareTo(BigDecimal.valueOf(flow.getNominalRange())) >= 0)
            .collect(Collectors.toList());

        System.out.println("Ini applicable flow ");
        System.out.println(applicableFlows);
    
        return applicableFlows.stream().map(flow -> {
            List<UserApp> usersInRole = userAppDb.findByRoleNames(Arrays.asList(flow.getApproverRole()));
            return new ApproverDisplay(flow.getApproverRole(), usersInRole);
        }).collect(Collectors.toList());
    }
    
    @Override
    public Invoice updateInvoice(Invoice invoiceFromDTO, List<Integer> selectedTaxIds) {
        Invoice invoice = getInvoiceById(invoiceFromDTO.getInvoiceId());
        Invoice dummy = getDummyInvoice();
        var listProduct = productService.getAllProductDummyInvoice(dummy);
        List<Product> newListProduct= new ArrayList<>();        
        if (invoice != null){
            invoice.setAccountName(invoiceFromDTO.getAccountName());
            invoice.setAccountNumber(invoiceFromDTO.getAccountNumber());
            // invoice.setAdditionalDocument(invoiceFromDTO.getAdditionalDocument());
            invoice.setBankName(invoiceFromDTO.getBankName());
            // invoice.setProductDocument(invoiceFromDTO.getProductDocument());
            invoice.setTotalDiscount(invoiceFromDTO.getTotalDiscount());
            invoice.setListProduct(invoiceFromDTO.getListProduct());
            invoice.setSignature(invoiceFromDTO.getSignature());
            invoice.setCity(invoiceFromDTO.getCity());
        }
        if (invoiceFromDTO.getDueDate() != null){
            invoice.setDueDate(invoiceFromDTO.getDueDate());
        }
        for (Product p : listProduct){
            p.setInvoice(invoice);
            newListProduct.add(p);
        }
        invoice.setListProduct(newListProduct);

        if(selectedTaxIds == null){
            invoice.getListTax().clear();
        } else {
            invoice.setListTax(taxDb.findAllById(selectedTaxIds));
        }
        calculateSubtotal(invoice);
        calculateDiscount(invoice);
        calculateTax(invoice);
        calculateGrandTotal(invoice);
        invoiceDb.save(invoice);
        return invoice;
    }

    public static boolean isNumeric(String str) {
        try {
            Double.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String checkValidityUpdate(UpdateInvoiceRequestDTO invoiceDTO){
        String accountNumber = invoiceDTO.getAccountNumber();
        String city = invoiceDTO.getCity();
        String res = "";
        if (!isNumeric(accountNumber)){
            res = "errorMessage, Please input the correct account number";
        }
        else if (isNumeric(city)){
            res = "errorMessage, Please input the correct city name";
        }
        return res;
    }

    @Override
    public void save(Invoice invoice){
        invoiceDb.save(invoice);
    }
}


    


