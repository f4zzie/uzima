// UZIMA BOREHOLE SYSTEM - BACKEND INTEGRATION GUIDE
// How to use the backend with your existing UI

package com.uzima;

import com.uzima.models.*;
import com.uzima.services.*;
import com.uzima.reports.*;
import java.util.List;

/**
 * QUICK START EXAMPLES FOR UI INTEGRATION
 */
public class BackendIntegrationGuide {

    // ============================================
    // 1. CLIENT REGISTRATION (from SignUpPage)
    // ============================================
    public static void exampleClientRegistration() {
        String fullName = "Jane Smith";
        String email = "jane@example.com";
        String phone = "+254700000001";
        String address = "456 Oak Avenue, Kisumu";
        String category = "Commercial";  // Industrial, Commercial, or Domestic
        String boreholeLocation = "Kisumu Industrial Area";
        String password = "securePassword123";
        
        // Register client with password
        boolean success = ClientService.registerClient(
            fullName, email, phone, address, category, boreholeLocation, password
        );
        
        if (success) {
            System.out.println("Client registered successfully!");
        } else {
            System.out.println("Registration failed - check email or input");
        }
    }

    // ============================================
    // 2. CLIENT LOGIN (from LoginPage)
    // ============================================
    public static void exampleClientLogin() {
        String email = "jane@example.com";
        
        // Retrieve client
        Client client = ClientService.getClientByEmail(email);
        
        if (client != null) {
            System.out.println("Client found: " + client.getFullName());
            System.out.println("Category: " + client.getCategory());
            System.out.println("Client ID: " + client.getClientId());
        } else {
            System.out.println("Client not found");
        }
    }

    // ============================================
    // 3. CREATE SERVICE REQUEST & INVOICE
    // ============================================
    public static void exampleCreateInvoice() {
        int clientId = 1;
        
        // Get client
        Client client = ClientService.getClientById(clientId);
        if (client == null) return;
        
        // Get fees for client category
        double[] fees = ServiceManagementService.getFeesByCategory(client.getCategory());
        double surveyFee = fees[0];
        double localAuthorityFee = fees[1];
        
        // Get service costs
        double drillingCost = InvoiceService.calculateDrillingCost("Symmetric", 150, 20);
        double pumpCost = ServiceManagementService.getPumpCost("Solar");
        double plumbingCost = 0;  // If no plumbing service
        double tankCost = 0;      // If no tank
        double depthHeightCharge = 0;  // Already included in drilling cost
        
        // Create invoice
        Invoice invoice = new Invoice(
            1,                  // requestId
            clientId,
            surveyFee,
            localAuthorityFee,
            drillingCost,
            pumpCost,
            plumbingCost,
            tankCost,
            depthHeightCharge
        );
        
        // Save invoice
        boolean saved = InvoiceService.createInvoice(invoice);
        if (saved) {
            System.out.println("Invoice created successfully!");
            System.out.println("Invoice ID: " + invoice.getInvoiceId());
            System.out.println("Total to pay: KES " + invoice.getTotalAmount());
            System.out.println("Tax (16%): KES " + invoice.getTaxAmount());
        }
    }

    // ============================================
    // 4. DISPLAY CLIENT DATA IN TABLE (UsersTable)
    // ============================================
    public static void exampleDisplayClientsInTable() {
        List<Client> clients = ClientService.getAllClients();
        
        // Display in your JTable
        for (Client client : clients) {
            System.out.printf("%d | %s | %s | %s | %s%n",
                client.getClientId(),
                client.getFullName(),
                client.getEmail(),
                client.getPhone(),
                client.getCategory()
            );
        }
    }

    // ============================================
    // 5. GENERATE REPORTS
    // ============================================
    public static void exampleGenerateReports() {
        // Client reports
        String clientsReport = ClientReportGenerator.generateAllClientsReport();
        String clientsByCategoryReport = ClientReportGenerator.generateClientsByCategoryReport();
        
        // Tax reports
        String taxSummary = TaxReport.generateTaxSummaryReport();
        String clientTaxReport = TaxReport.generateClientTaxReport(1);
        
        // Revenue reports
        String serviceRevenue = ServiceRevenueReport.generateServiceRevenueReport();
        String detailedRevenue = ServiceRevenueReport.generateDetailedRevenueReport();
        
        // Company reports
        String financialReport = CompanyRevenueReport.generateComprehensiveFinancialReport();
        String paymentStatus = CompanyRevenueReport.generatePaymentStatusReport();
        String feeCollectionReport = CompanyRevenueReport.generateFeeCollectionReport();
        
        // Display or save reports
        System.out.println(financialReport);
        System.out.println(paymentStatus);
    }

    // ============================================
    // 6. GET SERVICE INFORMATION
    // ============================================
    public static void exampleGetServiceInfo() {
        // Get all drilling services
        List<DrillingService> drillingServices = ServiceManagementService.getAllDrillingServices();
        for (DrillingService service : drillingServices) {
            System.out.println(service.getServiceType() + ": " + service.getDownPaymentFee() + " KES");
        }
        
        // Get all pump services
        List<PumpService> pumpServices = ServiceManagementService.getAllPumpServices();
        for (PumpService pump : pumpServices) {
            System.out.println(pump.getPumpType() + ": " + pump.getCost() + " KES");
        }
        
        // Get fees for a category
        double surveyFee = ServiceManagementService.getSurveyFee("Industrial");
        double localFee = ServiceManagementService.getLocalAuthorityFee("Industrial");
        System.out.println("Industrial - Survey: " + surveyFee + ", Local: " + localFee);
    }

    // ============================================
    // 7. UPDATE CLIENT & PAYMENT STATUS
    // ============================================
    public static void exampleUpdateData() {
        // Update client info
        ClientService.updateClient(1, "Jane Updated Name", "+254700000002", null, null);
        
        // Update invoice payment status
        InvoiceService.updatePaymentStatus(1, "Paid");
        
        // Get updated invoice
        Invoice invoice = InvoiceService.getInvoiceById(1);
        System.out.println("Payment Status: " + invoice.getPaymentStatus());
    }

    // ============================================
    // 8. GET CLIENT INVOICES
    // ============================================
    public static void exampleGetClientInvoices() {
        int clientId = 1;
        List<Invoice> invoices = InvoiceService.getClientInvoices(clientId);
        
        double totalOutstanding = 0;
        for (Invoice invoice : invoices) {
            System.out.printf("Invoice #%d | Amount: %.2f | Status: %s%n",
                invoice.getInvoiceId(),
                invoice.getTotalAmount(),
                invoice.getPaymentStatus()
            );
            
            if (!invoice.getPaymentStatus().equals("Paid")) {
                totalOutstanding += invoice.getTotalAmount();
            }
        }
        
        System.out.println("Total Outstanding: " + totalOutstanding);
    }

    // ============================================
    // 9. CALCULATE COSTS
    // ============================================
    public static void exampleCalculateCosts() {
        // Calculate drilling cost
        double drillingCost = InvoiceService.calculateDrillingCost("Core", 200, 30);
        System.out.println("Drilling Cost (Core, 200m depth, 30m height): " + drillingCost);
        
        // Calculate plumbing cost
        double plumbingCost = InvoiceService.calculatePlumbingCost(50, 500);
        System.out.println("Plumbing Cost (50m length @ 500/m): " + plumbingCost);
        
        // Calculate depth/height charge
        double depthCharge = InvoiceService.calculateDepthHeightCharge(150, 20);
        System.out.println("Depth/Height Charge (150m + 20m): " + depthCharge);
        
        // Calculate tax
        double subtotal = 200000;
        double tax = InvoiceService.calculateTax(subtotal);
        double total = InvoiceService.calculateTotalWithTax(subtotal);
        System.out.println("Subtotal: " + subtotal);
        System.out.println("Tax (16%): " + tax);
        System.out.println("Total: " + total);
    }

    // ============================================
    // 10. SEARCH & FILTER CLIENTS
    // ============================================
    public static void exampleSearchClients() {
        // Get all clients
        List<Client> allClients = ClientService.getAllClients();
        System.out.println("Total Clients: " + allClients.size());
        
        // Get clients by category
        List<Client> industrialClients = ClientService.getClientsByCategory("Industrial");
        System.out.println("Industrial Clients: " + industrialClients.size());
        
        // Search by email
        Client client = ClientService.getClientByEmail("jane@example.com");
        if (client != null) {
            System.out.println("Found: " + client.getFullName());
        }
    }
}

// ============================================
// INTEGRATION CHECKLIST
// ============================================
/*
 * 1. DATABASE SETUP
 *    ☐ Run database_schema.sql to create tables
 *    ☐ Update credentials in DatabaseConnection.java
 *    ☐ Test connection: DatabaseConnection.testConnection()
 *
 * 2. SIGNUPPAGE CHANGES
 *    ☐ Replace DBhelper.registerUser() with ClientService.registerClient()
 *    ☐ Update error messages from validation
 *
 * 3. LOGINPAGE CHANGES
 *    ☐ Replace DBhelper.loginUser() with ClientService.getClientByEmail()
 *    ☐ Store client ID for session
 *
 * 4. USERSTABLE CHANGES
 *    ☐ Replace manual queries with ClientService.getAllClients()
 *    ☐ Populate JTable with List<Client>
 *    ☐ Add filters using ClientService.getClientsByCategory()
 *
 * 5. NEW FEATURES TO ADD
 *    ☐ Create Service Request UI
 *    ☐ Display client invoices in a table
 *    ☐ Add payment status update buttons
 *    ☐ Generate and display reports
 *    ☐ Add invoice calculator UI
 *
 * 6. DEPENDENCIES
 *    ☐ MySQL JDBC driver is already in pom.xml
 *    ☐ No additional libraries needed
 */
