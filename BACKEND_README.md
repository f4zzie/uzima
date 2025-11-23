# UZIMA Borehole Drilling System - Backend Implementation

## Project Overview

A complete backend implementation for **UZIMA Borehole Drilling System** - a Java-based business management system for borehole drilling services in Kenya. The system manages clients, services, invoices, and generates comprehensive financial reports.

## Architecture Overview

```
┌─────────────────────────────────────────┐
│  UI Layer (Keep as-is)                  │
│  LoginPage.java, SignUpPage.java        │
│  UsersTable.java                        │
└─────────────────┬───────────────────────┘
                  │ calls
                  ↓
┌─────────────────────────────────────────┐
│  Service Layer (Business Logic)         │
│  ClientService.java                     │
│  InvoiceService.java                    │
│  ServiceManagementService.java          │
└─────────────────┬───────────────────────┘
                  │ uses
                  ↓
┌─────────────────────────────────────────┐
│  DAO Layer (Database Access)            │
│  ClientDAO.java                         │
│  ServiceDAO.java                        │
│  InvoiceDAO.java                        │
└─────────────────┬───────────────────────┘
                  │ uses
                  ↓
┌─────────────────────────────────────────┐
│  Models (Data Objects)                  │
│  Client, DrillingService, PumpService   │
│  PlumbingService, Invoice, etc.         │
└─────────────────┬───────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────┐
│  Database (MySQL)                       │
│  uzima_borehole                         │
└─────────────────────────────────────────┘
```

## File Structure

```
src/main/java/com/uzima/
├── database/
│   ├── DatabaseConnection.java       # Centralized connection manager
│   ├── ClientDAO.java                # Client CRUD operations
│   ├── ServiceDAO.java               # Service & fee lookups
│   └── InvoiceDAO.java               # Invoice & billing operations
├── models/
│   ├── Client.java                   # Client entity
│   ├── DrillingService.java          # Drilling service entity
│   ├── PumpService.java              # Pump service entity
│   ├── PlumbingService.java          # Plumbing service entity
│   ├── Invoice.java                  # Invoice entity with calculations
│   └── TaxCalculation.java           # Tax calculation utility (16%)
├── services/
│   ├── ClientService.java            # Client business logic
│   ├── InvoiceService.java           # Invoice & billing logic
│   └── ServiceManagementService.java # Service management logic
└── reports/
    ├── ClientReportGenerator.java    # Client data reports
    ├── TaxReport.java                # Tax collection reports
    ├── ServiceRevenueReport.java     # Service revenue reports
    └── CompanyRevenueReport.java     # Company financial reports

src/main/resources/
└── database_schema.sql               # Database initialization script
```

## Database Schema

### Tables Created:
- **clients** - Client information and categories
- **drilling_services** - Drilling service types and fees
- **pump_services** - Pump types and installation costs
- **plumbing_services** - Plumbing service details
- **tank_installation** - Tank capacity and costs
- **depth_height_charges** - Depth-based pricing
- **fees** - Survey and local authority fees by category
- **service_requests** - Service requests from clients
- **invoices** - Billing records with calculations
- **users** - User authentication

### Client Categories:
- Industrial (Survey: 20,000 KES | Local Authority: 50,000 KES)
- Commercial (Survey: 15,000 KES | Local Authority: 30,000 KES)
- Domestic (Survey: 7,000 KES | Local Authority: 10,000 KES)

## Features Implemented

### ✅ Client Management
- Register new clients with validation
- Store client details (name, email, phone, address, category, location)
- Update client information
- Retrieve clients by ID, email, or category
- Delete clients
- Count clients by category

### ✅ Service Management
- Retrieve drilling services (Symmetric, Core, Geo-Technical)
- Retrieve pump services (Submersible Electric, Solar, Hand)
- Get depth/height charges based on borehole depth
- Retrieve fees by client category
- Calculate plumbing costs

### ✅ Invoice & Billing
- Create invoices with all service costs
- Calculate depth/height charges
- Calculate plumbing costs
- Apply 16% tax automatically
- Calculate total amount to be paid
- Update payment status (Pending, Partial, Paid, Overdue)
- Retrieve invoices by client or invoice ID

### ✅ Reports
**Client Reports:**
- All clients with summary
- Clients by category
- Individual client profile

**Tax Reports:**
- Total tax collected
- Tax breakdown by invoice
- Tax report per client

**Revenue Reports:**
- Service revenue breakdown
- Drilling revenue
- Detailed invoice revenue

**Company Reports:**
- Comprehensive financial report
- Fee collection report
- Payment status summary

## Usage Examples

### 1. Register a New Client
```java
boolean success = ClientService.registerClient(
    "John Doe",                    // fullName
    "john@example.com",            // email
    "+254712345678",               // phone
    "123 Main Street, Nairobi",    // address
    "Industrial",                  // category
    "Karen, Nairobi"               // boreholeLocation
);
```

### 2. Create an Invoice
```java
// Get fees for client category
double[] fees = ServiceManagementService.getFeesByCategory("Industrial");
double surveyFee = fees[0];
double localAuthorityFee = fees[1];

// Calculate drilling cost (Symmetric drilling, 150m depth, 20m height)
double drillingCost = InvoiceService.calculateDrillingCost("Symmetric", 150, 20);

// Get pump cost (Solar pump)
double pumpCost = ServiceManagementService.getPumpCost("Solar");

// Create invoice
Invoice invoice = new Invoice(
    1,                  // requestId
    1,                  // clientId
    surveyFee,          // surveyFee
    localAuthorityFee,  // localAuthorityFee
    drillingCost,       // drillingCost
    pumpCost,           // pumpCost
    0,                  // plumbingCost
    0,                  // tankCost
    0                   // depthHeightCharge
);

InvoiceService.createInvoice(invoice);
System.out.println("Total to pay: KES " + invoice.getTotalAmount());
```

### 3. Generate Reports
```java
// Client report
System.out.println(ClientReportGenerator.generateAllClientsReport());
System.out.println(ClientReportGenerator.generateClientsByCategoryReport());

// Tax report
System.out.println(TaxReport.generateTaxSummaryReport());
System.out.println(TaxReport.generateClientTaxReport(clientId));

// Revenue report
System.out.println(ServiceRevenueReport.generateServiceRevenueReport());

// Company report
System.out.println(CompanyRevenueReport.generateComprehensiveFinancialReport());
System.out.println(CompanyRevenueReport.generatePaymentStatusReport());
```

## Database Setup

1. **Create the database:**
```sql
mysql -u root -p < src/main/resources/database_schema.sql
```

2. **Update connection credentials** in `DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/uzima_borehole";
private static final String DB_USER = "root";      // Change to your username
private static final String DB_PASSWORD = "teddy"; // Change to your password
```

## Service Rates

### Drilling Services (Down Payment)
- Symmetric drilling: 130,000 KES
- Core drilling: 225,000 KES
- Geo-Technical drilling: 335,000 KES

### Pump Installation
- Submersible Electric: 90,000 KES
- Solar: 65,000 KES
- Hand: 30,000 KES

### Depth & Height Charges (per meter)
- 1-100m: 1,000 KES/m
- 101-200m: 1,500 KES/m
- 201-300m: 2,000 KES/m
- 300m+: 2,500 KES/m

### Tax
- All invoices: 16% tax on subtotal

## Key Calculations

### Total Invoice Amount
```
Total = (Survey Fee + Local Authority Fee + Drilling Cost + Pump Cost + 
         Plumbing Cost + Tank Cost + Depth/Height Charge) × 1.16
```

### Example Calculation
```
Industrial Client - Symmetric Drilling, 150m depth, 20m height, Solar pump:

Survey Fee:           20,000
Local Authority Fee:  50,000
Drilling Cost:       130,000 + (170m × 1,500) = 385,000
Pump Cost:            65,000
─────────────────────────────
Subtotal:            520,000
Tax (16%):            83,200
─────────────────────────────
TOTAL:               603,200 KES
```

## Integration with UI

The UI components (LoginPage, SignUpPage, UsersTable) can call the service layer directly:

```java
// Instead of direct database calls:
// Before: DBhelper.loginUser(email, password);

// After: Use the service layer:
Client client = ClientService.getClientByEmail(email);

// Get client invoices:
List<Invoice> invoices = InvoiceService.getClientInvoices(clientId);

// Generate report:
String report = ClientReportGenerator.generateAllClientsReport();
```

## Testing Notes

The system is designed with:
- ✅ Input validation at service layer
- ✅ Null checks in DAO layer
- ✅ Proper error handling and logging
- ✅ Transaction consistency
- ✅ Separation of concerns

## Dependencies

```xml
<!-- Already in pom.xml -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

## Future Enhancements

- [ ] Payment processing integration
- [ ] Email notifications for invoices
- [ ] Maintenance service tracking
- [ ] Pump maintenance scheduling
- [ ] Advanced filtering in reports
- [ ] Export reports to PDF/Excel
- [ ] User role-based access control
- [ ] Audit logging

## Support

For questions or issues, refer to:
1. Database schema: `src/main/resources/database_schema.sql`
2. Model classes for entity relationships
3. Service classes for business logic examples
4. Report generators for output formatting
