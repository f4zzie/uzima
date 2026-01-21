# 💧 UZIMA BOREHOLE MANAGEMENT SYSTEM

## Complete Technical Documentation

**Version:** 1.0.0  
**Date:** January 21, 2026  
**Technology Stack:** Java 11, Swing, MySQL, Maven, HikariCP

---

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Application Entry Point](#application-entry-point)
3. [Architecture Diagram](#architecture-diagram)
4. [Utility Classes](#utility-classes)
5. [Model Classes (POJOs)](#model-classes-pojos)
6. [Data Access Objects (DAO)](#data-access-objects-dao)
7. [User Interface Components](#user-interface-components)
8. [Service & Report Classes](#service--report-classes)
9. [Application Flow](#application-flow)
10. [Database Schema](#database-schema)
11. [Pricing Tables](#pricing-tables)

---

## System Overview

UZIMA Borehole Management System is a desktop application for managing borehole drilling services in Kenya. The system supports:

- **Staff Portal:** Admin, Manager, and Clerk roles for managing clients, transactions, and reports
- **Customer Portal:** Registered clients can view their services and request new ones
- **Service Management:** Drilling, pump installation, tank installation, plumbing, and maintenance
- **Financial Tracking:** Invoicing, payments, and revenue reports

---

## Application Entry Point

### `MainApplication.java`

```java
package com.uzima;

public class MainApplication {
    public static void main(String[] args)
}
```

| Method | Description |
|--------|-------------|
| `main(String[] args)` | Entry point that initializes the application |

**Execution Flow:**

1. **Set Look and Feel:** `UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())` - Applies system-native UI styling
2. **Test Database:** `DatabaseConnection.testConnection()` - Verifies MySQL connection
3. **Launch UI:** `SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true))` - Opens login form on Event Dispatch Thread
4. **Error Handling:** Shows error dialog if database is unavailable

**Dependencies Used:**
- `javax.swing.UIManager` - For look and feel
- `javax.swing.SwingUtilities` - For thread-safe UI initialization
- `javax.swing.JOptionPane` - For error dialogs
- `com.uzima.ui.LoginForm` - Main login interface
- `com.uzima.util.DatabaseConnection` - Database connectivity

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER                             │
│  ┌─────────────┐    ┌─────────────────┐    ┌──────────────────────┐     │
│  │  LoginForm  │───→│  DashboardForm  │    │  CustomerDashboard   │     │
│  │             │    │    (Admin)      │    │    (Customer)        │     │
│  └─────────────┘    └─────────────────┘    └──────────────────────┘     │
│         │                   │                        │                   │
│         ▼                   ▼                        ▼                   │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  ClientManagementForm │ ViewTransactionsForm │ ReportsForm │ etc │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           UTILITY LAYER                                  │
│  ┌─────────────────┐  ┌──────────────┐  ┌─────────────────────────┐     │
│  │  SessionManager │  │   UIHelper   │  │  DatabaseConnection     │     │
│  │  (Singleton)    │  │ (Formatting) │  │  (HikariCP Pool)        │     │
│  └─────────────────┘  └──────────────┘  └─────────────────────────┘     │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         DATA ACCESS LAYER (DAO)                          │
│  ┌──────────┐  ┌────────────┐  ┌────────────────┐  ┌──────────────┐     │
│  │ UserDAO  │  │ ClientDAO  │  │ TransactionDAO │  │  ServiceDAO  │     │
│  └──────────┘  └────────────┘  └────────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           MODEL LAYER (POJOs)                            │
│  ┌──────────┐  ┌────────────┐  ┌─────────────┐  ┌───────────────┐       │
│  │   User   │  │   Client   │  │ Transaction │  │  ServiceType  │       │
│  └──────────┘  └────────────┘  └─────────────┘  └───────────────┘       │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           DATABASE LAYER                                 │
│                        MySQL: uzima_borehole_db                          │
│  Tables: users, clients, transactions, services, invoices, payments     │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Utility Classes

### `com.uzima.util.DatabaseConnection`

**Purpose:** Manages database connections using HikariCP connection pooling for thread-safe, high-performance database access.

```java
public class DatabaseConnection {
    private static HikariDataSource dataSource;
    private static final String URL = "jdbc:mysql://localhost:3306/uzima_borehole_db";
    private static final String USER = "root";
    private static final String PASSWORD = "kali";
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getConnection()` | `Connection` | Returns a database connection from the pool |
| `closePool()` | `void` | Closes the connection pool (for application shutdown) |
| `testConnection()` | `boolean` | Tests if the database connection is working |

**Configuration:**
- Maximum pool size: 10 connections
- Connection timeout: 30 seconds
- Idle timeout: 600 seconds
- Max lifetime: 1800 seconds

---

### `com.uzima.util.SessionManager`

**Purpose:** Singleton class that manages user session state throughout the application lifecycle.

```java
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Date sessionStartTime;
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getInstance()` | `SessionManager` | Returns the singleton instance |
| `login(User user)` | `void` | Sets the logged-in user and records session start time |
| `getCurrentUser()` | `User` | Returns the currently logged-in user |
| `isLoggedIn()` | `boolean` | Checks if a user is logged in |
| `hasRole(String role)` | `boolean` | Checks if user has a specific role |
| `isAdmin()` | `boolean` | Checks if current user is ADMIN |
| `isAdminOrManager()` | `boolean` | Checks if user is ADMIN or MANAGER |
| `logout()` | `void` | Clears the session |
| `getSessionDuration()` | `long` | Returns session duration in milliseconds |

---

### `com.uzima.util.UIHelper`

**Purpose:** Utility class providing common Swing UI operations, styling, and formatting methods.

```java
public class UIHelper {
    // Color Constants
    public static final Color PRIMARY_COLOR = new Color(25, 118, 210);   // Blue
    public static final Color SUCCESS_COLOR = new Color(46, 125, 50);    // Green
    public static final Color WARNING_COLOR = new Color(237, 108, 2);    // Orange
    public static final Color DANGER_COLOR = new Color(211, 47, 47);     // Red
    
    // Font Constants
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `formatCurrency(double amount)` | `String` | Formats as "KSh X,XXX.XX" |
| `formatDate(Date date)` | `String` | Formats as "dd/MM/yyyy" |
| `formatDateTime(Date date)` | `String` | Formats as "dd/MM/yyyy HH:mm:ss" |
| `parseDate(String dateStr)` | `Date` | Parses string to Date object |
| `showInfo(Component parent, String message)` | `void` | Shows information dialog |
| `showWarning(Component parent, String message)` | `void` | Shows warning dialog |
| `showError(Component parent, String message)` | `void` | Shows error dialog |
| `showConfirm(Component parent, String message)` | `boolean` | Shows confirmation dialog |
| `stylePrimaryButton(JButton btn)` | `void` | Applies primary blue styling |
| `styleSuccessButton(JButton btn)` | `void` | Applies green success styling |
| `styleDangerButton(JButton btn)` | `void` | Applies red danger styling |
| `styleTable(JTable table)` | `void` | Applies consistent table styling |
| `centerFrame(JFrame frame)` | `void` | Centers frame on screen |

---

## Model Classes (POJOs)

### `com.uzima.model.User`

**Purpose:** Represents system users (staff members) with authentication and role information.

```java
public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role;           // ADMIN, MANAGER, CLERK
    private String email;
    private String phone;
    private boolean isActive;
    private Date createdAt;
    private Date lastLogin;
}
```

| Field | Type | Description |
|-------|------|-------------|
| `userId` | `int` | Primary key (auto-increment) |
| `username` | `String` | Unique login username |
| `password` | `String` | User password |
| `fullName` | `String` | User's full name |
| `role` | `String` | Role: ADMIN, MANAGER, or CLERK |
| `email` | `String` | Email address |
| `phone` | `String` | Phone number |
| `isActive` | `boolean` | Account status (active/inactive) |
| `createdAt` | `Date` | Account creation timestamp |
| `lastLogin` | `Date` | Last login timestamp |

**Constructors:**
- `User()` - Default constructor
- `User(String username, String password, String fullName, String role)` - Parameterized constructor

---

### `com.uzima.model.Client`

**Purpose:** Represents borehole clients/customers with contact and location information.

```java
public class Client {
    private String clientId;           // UZM-IND-001, UZM-COM-002, UZM-DOM-003
    private String fullName;
    private String physicalAddress;
    private String phoneNumber;
    private String phoneNumber2;
    private String email;
    private String clientCategory;     // INDUSTRIAL, COMMERCIAL, DOMESTIC
    private String boreholeLocation;
    private String county;
    private String gpsCoordinates;
    private String idNumber;
    private String password;           // For customer portal login
    private Date registrationDate;
    private boolean isActive;
    private int createdBy;
}
```

| Field | Type | Description |
|-------|------|-------------|
| `clientId` | `String` | Unique ID (format: UZM-XXX-NNN) |
| `fullName` | `String` | Client's full name |
| `physicalAddress` | `String` | Physical/postal address |
| `phoneNumber` | `String` | Primary contact number |
| `phoneNumber2` | `String` | Secondary contact number |
| `email` | `String` | Email address |
| `clientCategory` | `String` | INDUSTRIAL, COMMERCIAL, or DOMESTIC |
| `boreholeLocation` | `String` | Intended borehole site location |
| `county` | `String` | Kenya county (47 counties) |
| `gpsCoordinates` | `String` | GPS coordinates of site |
| `idNumber` | `String` | National ID number |
| `password` | `String` | Password for customer portal |
| `registrationDate` | `Date` | When client registered |
| `isActive` | `boolean` | Active status |
| `createdBy` | `int` | User ID who created this client |

**Client ID Prefixes:**
- `UZM-IND-XXX` - Industrial clients
- `UZM-COM-XXX` - Commercial clients
- `UZM-DOM-XXX` - Domestic clients

---

### `com.uzima.model.Transaction`

**Purpose:** Represents client transactions/service orders with all cost breakdowns.

```java
public class Transaction {
    // Identity
    private int transactionId;
    private String transactionNo;      // TXN-2026-0001
    private String clientId;
    private String invoiceNo;
    
    // Drilling Details
    private String drillingType;       // SYMMETRIC, CORE, GEO_TECHNICAL
    private double boreholeDepth;
    private double drillingCost;
    
    // Pump Details
    private String pumpType;           // SUBMERSIBLE, SOLAR, HAND
    private double tankHeight;
    private double pumpCost;
    
    // Tank Details
    private int tankCapacity;          // In litres
    private double tankCost;
    
    // Plumbing Details
    private double plumbingLength;
    private double plumbingCost;
    
    // Fees
    private double surveyFee;
    private double localAuthorityFee;
    
    // Totals
    private double subtotal;
    private double taxAmount;          // 16% VAT
    private double grandTotal;
    
    // Payment
    private double amountPaid;
    private double balance;
    private String paymentStatus;      // PENDING, PARTIAL, PAID
    
    // Status
    private String jobStatus;          // PENDING_SURVEY, APPROVED, IN_PROGRESS, COMPLETED
    private Date transactionDate;
}
```

---

## Data Access Objects (DAO)

### `com.uzima.dao.UserDAO`

**Purpose:** Data access object for all User-related database operations.

```java
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `authenticate(String username, String password)` | `User` | Validates credentials, returns User or null |
| `updateLastLogin(int userId)` | `void` | Updates user's last login timestamp |
| `getAllUsers()` | `List<User>` | Returns list of all users |
| `addUser(User user)` | `boolean` | Inserts new user into database |
| `updateUser(User user)` | `boolean` | Updates existing user details |
| `changePassword(int userId, String newPassword)` | `boolean` | Changes user password |
| `updatePassword(int userId, String newPassword)` | `boolean` | Alias for changePassword |
| `getUserById(int userId)` | `User` | Retrieves user by ID |
| `deleteUser(int userId)` | `boolean` | Deletes user by ID |
| `usernameExists(String username)` | `boolean` | Checks if username is taken |
| `mapResultSetToUser(ResultSet rs)` | `User` | Maps DB result to User object |

---

### `com.uzima.dao.ClientDAO`

**Purpose:** Data access object for all Client-related database operations.

```java
public class ClientDAO {
    private static final Logger logger = LoggerFactory.getLogger(ClientDAO.class);
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `generateClientId(String category)` | `String` | Generates unique client ID |
| `addClient(Client client)` | `boolean` | Inserts new client into database |
| `updateClient(Client client)` | `boolean` | Updates existing client |
| `deleteClient(String clientId)` | `boolean` | Deletes client by ID |
| `getClientById(String clientId)` | `Client` | Retrieves client by ID |
| `getAllClients()` | `List<Client>` | Returns all clients |
| `getClientsByCategory(String category)` | `List<Client>` | Gets clients by category |
| `searchClients(String searchTerm)` | `List<Client>` | Searches clients by name/phone |
| `getTotalClientCount()` | `int` | Returns total client count |
| `authenticateCustomer(String clientId, String password)` | `Client` | Customer portal authentication |
| `mapResultSetToClient(ResultSet rs)` | `Client` | Maps DB result to Client object |

---

### `com.uzima.dao.TransactionDAO`

**Purpose:** Data access object for transaction/service order operations.

| Method | Return Type | Description |
|--------|-------------|-------------|
| `generateTransactionNo()` | `String` | Generates unique transaction number |
| `addTransaction(Transaction txn)` | `boolean` | Creates new transaction |
| `updateTransaction(Transaction txn)` | `boolean` | Updates existing transaction |
| `getTransactionsByClient(String clientId)` | `List<Transaction>` | Gets client's transactions |
| `getAllTransactions()` | `List<Transaction>` | Returns all transactions |
| `updatePayment(int transactionId, double amount)` | `boolean` | Records payment |

---

## User Interface Components

### `com.uzima.ui.LoginForm`

**Purpose:** Professional dual-portal login form for Staff and Customer access.

```java
public class LoginForm extends JFrame {
    private JTextField txtUsername, txtClientId;
    private JPasswordField txtPassword, txtCustomerPassword;
    private JTabbedPane tabbedPane;
    private UserDAO userDAO;
    private ClientDAO clientDAO;
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `initComponents()` | `void` | Initializes the login UI |
| `createLogoPanel()` | `JPanel` | Creates branding section |
| `createLoginTabs()` | `JTabbedPane` | Creates Staff/Customer tabs |
| `createStaffLoginPanel()` | `JPanel` | Staff login form |
| `createCustomerLoginPanel()` | `JPanel` | Customer login form |
| `handleStaffLogin()` | `void` | Authenticates staff user |
| `handleCustomerLogin()` | `void` | Authenticates customer |
| `openCustomerRegistration()` | `void` | Opens registration form |

**Authentication Flow:**
1. Staff enters username/password → `UserDAO.authenticate()` → Opens `DashboardForm`
2. Customer enters clientId/password → `ClientDAO.authenticateCustomer()` → Opens `CustomerDashboard`

---

### `com.uzima.ui.DashboardForm`

**Purpose:** Admin dashboard with sidebar navigation, statistics, and recent activity.

```java
public class DashboardForm extends JFrame {
    private JLabel lblTotalClients, lblActiveProjects, lblMonthlyRevenue, lblPendingPayments;
    private JTable recentTransactionsTable;
    private Timer clockTimer;
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `initComponents()` | `void` | Initializes dashboard layout |
| `createSidebar()` | `JPanel` | Creates navigation sidebar |
| `createTopHeader()` | `JPanel` | Creates header with welcome and clock |
| `createMainContent()` | `JPanel` | Creates main content area |
| `createStatsCards()` | `JPanel` | Creates statistics cards |
| `loadDashboardData()` | `void` | Loads stats and recent transactions |
| `startClock()` | `void` | Starts live clock timer |
| `openClientManagement()` | `void` | Opens client management form |
| `openServiceRequests()` | `void` | Opens service requests dialog |
| `openViewTransactions()` | `void` | Opens transactions view |
| `openReports()` | `void` | Opens reports form |
| `openUserManagement()` | `void` | Opens user management form |
| `handleLogout()` | `void` | Logs out and returns to login |

**Sidebar Menu Items:**
- 🏠 Dashboard
- 👥 Clients
- 📨 Service Requests
- 📋 Transactions
- 💳 Payments
- 🔧 Services
- 📊 Reports
- 👤 User Management
- ⚙️ Settings
- 🚪 Logout

---

### `com.uzima.ui.CustomerDashboard`

**Purpose:** Customer portal for registered clients to view services and request new ones.

```java
public class CustomerDashboard extends JFrame {
    private Client currentClient;
    private JLabel lblTotalServices, lblAmountPaid, lblPendingBalance, lblActiveRequests;
    private JTable transactionsTable;
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `initComponents()` | `void` | Initializes customer dashboard |
| `createSidebar()` | `JPanel` | Creates customer navigation |
| `createProfileSection()` | `JPanel` | Displays customer profile |
| `createTopHeader()` | `JPanel` | Header with welcome and clock |
| `createMainContent()` | `JPanel` | Main content with stats |
| `loadCustomerData()` | `void` | Loads customer-specific data |
| `showMyServices()` | `void` | Shows customer's services |
| `openServiceRequest()` | `void` | Opens new service request form |
| `showPaymentHistory()` | `void` | Displays payment history |
| `showInvoices()` | `void` | Shows customer invoices |
| `handleLogout()` | `void` | Signs out and returns to login |

**Customer Menu Items:**
- 🏠 Dashboard
- 📋 My Services
- 🛒 Request New Service
- 💳 Payment History
- 📄 Invoices
- 👤 My Profile
- ⚙️ Settings
- 🚪 Logout

---

### `com.uzima.ui.CustomerServiceRequestForm`

**Purpose:** Form for customers to request borehole services with real-time pricing.

```java
public class CustomerServiceRequestForm extends JDialog {
    private Client currentClient;
    
    // Pricing Constants (from official tables)
    private static final int DRILLING_SYMMETRIC = 130000;
    private static final int DRILLING_CORE = 225000;
    private static final int DRILLING_GEO_TECHNICAL = 335000;
    
    private static final int PUMP_SUBMERSIBLE_ELECTRIC = 90000;
    private static final int PUMP_SOLAR = 65000;
    private static final int PUMP_HAND = 30000;
    
    private static final double TAX_RATE = 0.16;
}
```

| Method | Return Type | Description |
|--------|-------------|-------------|
| `initComponents()` | `void` | Initializes form layout |
| `createServicesPanel()` | `JPanel` | Creates service selection panel |
| `createDrillingSection()` | `JPanel` | Drilling options (type, depth) |
| `createPumpSection()` | `JPanel` | Pump options (type, height) |
| `createTankSection()` | `JPanel` | Tank capacity selection |
| `createPlumbingSection()` | `JPanel` | Plumbing options |
| `createSummaryPanel()` | `JPanel` | Cost summary panel |
| `calculateTotal()` | `void` | Real-time cost calculation |
| `getSurveyFee()` | `int` | Gets survey fee by category |
| `getLocalAuthorityFee()` | `int` | Gets local authority fee |
| `calculateDepthHeightCost(int meters)` | `int` | Calculates depth/height cost using Table 4 |
| `submitRequest()` | `void` | Submits service request |

---

## Service & Report Classes

### `com.uzima.service.InvoiceService`

**Purpose:** Service class for invoice generation and management.

| Method | Return Type | Description |
|--------|-------------|-------------|
| `generateInvoice(Transaction txn)` | `String` | Generates invoice number |
| `getAllTransactions()` | `List<Transaction>` | Gets all transactions for reports |

---

### `com.uzima.report.ServiceRevenueReport`

**Purpose:** Generates revenue reports by service type.

| Method | Return Type | Description |
|--------|-------------|-------------|
| `generateServiceRevenueReport()` | `String` | Revenue breakdown by service |
| `generateAverageTransactionReport()` | `String` | Transaction statistics and averages |

---

## Application Flow

### 1. Application Startup
```
MainApplication.main()
    └─→ UIManager.setLookAndFeel() - Set native look and feel
    └─→ DatabaseConnection.testConnection() - Test MySQL connection
        ├─→ Success: SwingUtilities.invokeLater() → LoginForm
        └─→ Failure: JOptionPane.showMessageDialog() - Error dialog
```

### 2. Staff Login Flow
```
LoginForm
    └─→ handleStaffLogin()
        └─→ UserDAO.authenticate(username, password)
            ├─→ Success: 
            │   └─→ SessionManager.login(user)
            │   └─→ DashboardForm.setVisible(true)
            │   └─→ LoginForm.dispose()
            └─→ Failure: Show error message
```

### 3. Customer Login Flow
```
LoginForm
    └─→ handleCustomerLogin()
        └─→ ClientDAO.authenticateCustomer(clientId, password)
            ├─→ Success: 
            │   └─→ CustomerDashboard(client).setVisible(true)
            │   └─→ LoginForm.dispose()
            └─→ Failure: Show error message
```

### 4. Service Request Flow
```
CustomerDashboard
    └─→ openServiceRequest()
        └─→ CustomerServiceRequestForm(this, currentClient)
            └─→ Select services (drilling, pump, tank, plumbing)
            └─→ calculateTotal() - Real-time pricing
            └─→ submitRequest() - Submit for admin review
```

### 5. Admin Service Approval Flow
```
DashboardForm
    └─→ openServiceRequests()
        └─→ showServiceRequestsDialog()
            ├─→ View Details - showRequestDetails()
            ├─→ Approve - approveRequest()
            ├─→ Reject - rejectRequest()
            └─→ Convert to Transaction - convertToTransaction()
```

---

## Database Schema

### Tables Overview

| Table | Description |
|-------|-------------|
| `users` | System users (admin, manager, clerk) |
| `clients` | Registered clients/customers |
| `transactions` | Service orders and billing |
| `services` | Available service types |
| `invoices` | Generated invoices |
| `payments` | Payment records |

### Key Relationships
- `transactions.client_id` → `clients.client_id`
- `transactions.created_by` → `users.user_id`
- `invoices.transaction_id` → `transactions.transaction_id`
- `payments.transaction_id` → `transactions.transaction_id`

---

## Pricing Tables

### Table 1: Survey and Local Authority Fees

| Client Category | Survey Fees (KSh) | Local Authority Fees (KSh) |
|-----------------|-------------------|---------------------------|
| Industrial | 20,000 | 50,000 |
| Commercial | 15,000 | 30,000 |
| Domestic | 7,000 | 10,000 |

### Table 2: Drilling Services (Down Payment)

| Type of Drilling | Cost (KSh) |
|------------------|------------|
| Symmetric Drilling | 130,000 |
| Core Drilling | 225,000 |
| Geo-Technical Drilling | 335,000 |

### Table 3: Pump Installation

| Type of Pump | Cost (KSh) |
|--------------|------------|
| Submersible Electric Pump | 90,000 |
| Solar Pump | 65,000 |
| Hand Pump | 30,000 |

### Table 4: Depth/Height Cost per Meter

| Depth/Height Range | Cost per Meter (KSh) |
|--------------------|---------------------|
| 1 - 100 m | 1,000 |
| 101 - 200 m | 1,500 |
| 201 - 300 m | 2,000 |
| Over 300 m | 2,500 |

### Tax
- **VAT:** 16% on total cost of service

---

## Project Structure

```
uzima/
├── pom.xml                              # Maven configuration
├── database/
│   ├── schema.sql                       # Database schema
│   └── add_test_customer.sql            # Test data
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── uzima/
│       │           ├── Main.java                    # Alternative entry point
│       │           ├── MainApplication.java         # Primary entry point
│       │           ├── dao/
│       │           │   ├── ClientDAO.java
│       │           │   ├── ServiceDAO.java
│       │           │   ├── TransactionDAO.java
│       │           │   └── UserDAO.java
│       │           ├── model/
│       │           │   ├── Client.java
│       │           │   ├── ServiceType.java
│       │           │   ├── Transaction.java
│       │           │   └── User.java
│       │           ├── report/
│       │           │   └── ServiceRevenueReport.java
│       │           ├── service/
│       │           │   └── InvoiceService.java
│       │           ├── ui/
│       │           │   ├── ClientManagementForm.java
│       │           │   ├── CustomerDashboard.java
│       │           │   ├── CustomerRegistrationForm.java
│       │           │   ├── CustomerServiceRequestForm.java
│       │           │   ├── DashboardForm.java
│       │           │   ├── LoginForm.java
│       │           │   ├── NewTransactionForm.java
│       │           │   ├── ReportsForm.java
│       │           │   ├── ServicesManagementForm.java
│       │           │   ├── UserManagementForm.java
│       │           │   └── ViewTransactionsForm.java
│       │           └── util/
│       │               ├── DatabaseConnection.java
│       │               ├── SessionManager.java
│       │               └── UIHelper.java
│       └── resources/
└── target/                              # Compiled classes
```

---

## Running the Application

### Prerequisites
- Java 11 or higher
- MySQL Server
- Maven

### Setup
1. Create MySQL database: `CREATE DATABASE uzima_borehole_db;`
2. Run schema: `mysql -u root -p uzima_borehole_db < database/schema.sql`
3. Compile: `mvn clean compile`
4. Run: `mvn exec:java -Dexec.mainClass="com.uzima.MainApplication"`

### Default Credentials
- **Admin:** admin / admin123
- **Test Customer:** DOM-2025-0001 / test123

---

**© 2026 UZIMA Borehole Company - All Rights Reserved**
