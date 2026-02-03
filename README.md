# UZIMA BOREHOLE DRILLING SYSTEM

## 📋 Project Overview
A comprehensive Java Swing-based desktop application for managing borehole drilling services, client management, billing, and reporting for Uzima Borehole Company.

## 🎯 Features Implemented

### ✅ 1. User Management & Authentication
- Multi-role login system (Admin, Manager, Clerk)
- Secure session management
- Password authentication
- User activity tracking

**Default Login Credentials:**
- Username: `admin` | Password: `admin123`
- Username: `manager` | Password: `manager123`
- Username: `clerk` | Password: `clerk123`

### ✅ 2. Client Management Module
- Register new clients with complete information
- Auto-generated unique client IDs (UZM-IND-001, UZM-COM-001, UZM-DOM-001)
- Client categories: Industrial, Commercial, Domestic
- Search and filter clients
- Update and delete client records
- GPS coordinates support
- Borehole location tracking

### ✅ 3. Service Registration
- **Drilling Services:**
  - Symmetric Drilling (KSh 130,000 down payment)
  - Core Drilling (KSh 225,000 down payment)
  - Geo-Technical Drilling (KSh 335,000 down payment)

- **Pump Installation:**
  - Submersible Electric Pump (KSh 90,000)
  - Solar Pump (KSh 65,000)
  - Hand Pump (KSh 30,000)

- **Tank Installation:**
  - Multiple capacity options (1000L, 2000L, 5000L, 10000L)
  - Height-based pricing

- **Plumbing Services:**
  - Manual cost entry
  - Support for pipe types, diameters, lengths, outlets

### ✅ 4. Automated Billing & Calculations

#### Depth/Height Cost Calculation (Tiered Pricing):
- 1-100 meters: KSh 1,000/meter
- 101-200 meters: KSh 1,500/meter
- 201-300 meters: KSh 2,000/meter
- Over 300 meters: KSh 2,500/meter

#### Survey & Authority Fees:
| Client Category | Survey Fee | Authority Fee |
|-----------------|------------|---------------|
| Industrial      | KSh 20,000 | KSh 50,000    |
| Commercial      | KSh 15,000 | KSh 30,000    |
| Domestic        | KSh 7,000  | KSh 10,000    |

#### Cost Formula:
```
Drilling Cost = Down Payment + (Depth × Rate per meter)
Pump Cost = Base Cost + (Height × Rate per meter)
Subtotal = Drilling + Pump + Tank + Plumbing + Survey Fee + Authority Fee
Tax = Subtotal × 16%
Grand Total = Subtotal + Tax
```

### ✅ 5. Transaction Management
- Create new service transactions
- Auto-generated transaction numbers (TXN-YYYYMMDD-0001)
- Real-time cost calculations
- Multiple services per transaction
- Payment status tracking (Not Paid, Partially Paid, Paid)
- Job status tracking (Pending Survey, Approved, In Progress, Completed)
- Transaction notes and documentation

### ✅ 6. Dashboard
- Welcome screen with user information
- Quick access to all modules
- Color-coded menu system
- Statistics display (prepared for implementation)

### ✅ 7. Comprehensive Report Generation (NEW!)
- **9 Different Report Types:**
  - All Clients Report
  - Clients by Category
  - Financial Report
  - Fee Collection Report
  - Payment Status Summary
  - Service Revenue Report
  - Plumbing Revenue Report
  - Average Transaction Report
  - Tax Report

- **Export Formats:**
  - **PDF Export** - Professional formatted PDF documents with headers, footers, and styling
  - **Word Export (.DOCX)** - Microsoft Word compatible documents
  - **Print** - Direct printing to system printer

- **Features:**
  - Automatic timestamp on all reports
  - Professional formatting with company branding
  - One-click export with suggested filenames
  - Optional "Open After Save" functionality
  - Real-time data from database

📄 **See [REPORT_GENERATION_GUIDE.md](REPORT_GENERATION_GUIDE.md) for detailed usage instructions**

### ✅ 8. Database Design
- 13 normalized database tables
- Views for reporting
- Stored functions for calculations
- Audit logging capability
- Referential integrity with foreign keys

## 🛠️ Technology Stack

- **Language:** Java 11
- **GUI Framework:** Java Swing
- **Database:** MySQL 8.0
- **Build Tool:** Maven
- **Connection Pooling:** HikariCP
- **PDF Generation:** iText 5.5.13.3 (fully implemented)
- **Word Export:** Apache POI 5.2.3 (fully implemented)
- **Date Picker:** JCalendar
- **Logging:** SLF4J + Logback

## 📦 Project Structure

```
uzima/
├── database/
│   └── uzima_db_setup.sql          # Complete database schema
├── src/main/java/com/uzima/
│   ├── Main.java                    # Application entry point
│   ├── MainApplication.java         # Alternative entry point
│   ├── dao/                         # Data Access Objects
│   │   ├── ClientDAO.java
│   │   ├── TransactionDAO.java
│   │   └── UserDAO.java
│   ├── model/                       # Data models
│   │   ├── Client.java
│   │   ├── Transaction.java
│   │   └── User.java
│   ├── ui/                          # User Interface forms
│   │   ├── ClientManagementForm.java
│   │   ├── DashboardForm.java
│   │   ├── LoginForm.java
│   │   ├── NewTransactionForm.java
│   │   ├── ReportsForm.java
│   │   ├── ServicesManagementForm.java
│   │   ├── UserManagementForm.java
│   │   └── ViewTransactionsForm.java
│   └── util/                        # Utility classes
│       ├── DatabaseConnection.java
│       ├── SessionManager.java
│       └── UIHelper.java
├── pom.xml                          # Maven dependencies
└── README.md                        # This file
```

## 🚀 Setup & Installation

### Prerequisites
1. **Java Development Kit (JDK) 11 or higher**
   ```bash
   java -version
   ```

2. **MySQL Server 8.0 or higher**
   ```bash
   mysql --version
   ```

3. **Maven** (for building)
   ```bash
   mvn -version
   ```

### Installation Steps

#### 1. Database Setup

**Start MySQL:**
```bash
sudo systemctl start mysql
# or
sudo service mysql start
```

**Create Database:**
```bash
mysql -u root -p
```

Then run the SQL script:
```bash
mysql -u root -p < database/uzima_db_setup.sql
```

Or manually:
```sql
source /path/to/uzima/database/uzima_db_setup.sql;
```

**Verify Database:**
```sql
USE uzima_borehole_db;
SHOW TABLES;
SELECT * FROM users;
```

#### 2. Configure Database Connection

Edit `src/main/java/com/uzima/util/DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/uzima_borehole_db?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD"; // Change this
```

#### 3. Build the Project

```bash
cd /home/kali/Desktop/uzima2/uzima
mvn clean install
```

#### 4. Run the Application

**Option 1: Using Maven**
```bash
mvn exec:java -Dexec.mainClass="com.uzima.Main"
```

**Option 2: Using compiled JAR**
```bash
java -jar target/uzima-borehole-system-1.0.0.jar
```

**Option 3: From IDE (IntelliJ IDEA, Eclipse, NetBeans)**
- Open the project
- Run `Main.java` or `MainApplication.java`

## 📖 User Guide

### Login
1. Launch the application
2. Enter username and password
3. Click **LOGIN**

### Register a Client
1. From Dashboard, click **Manage Clients**
2. Click **NEW** button
3. Fill in client details:
   - Full Name (required)
   - Physical Address (required)
   - Phone Number (required)
   - Select Client Category
   - Borehole Location (required)
   - Optional: Email, County, GPS Coordinates, ID Number
4. Click **SAVE**

### Create a Transaction
1. From Dashboard, click **New Transaction**
2. Select client from dropdown
3. Select services required:
   - **Drilling:** Check box, select type, enter depth
   - **Pump:** Check box, select type, enter tank height
   - **Tank:** Check box, select capacity
   - **Plumbing:** Check box, enter cost
4. Click **CALCULATE TOTAL** to see breakdown
5. Review all costs:
   - Survey Fee (auto-filled)
   - Authority Fee (auto-filled)
   - Subtotal
   - Tax (16%)
   - Grand Total
6. Add notes if needed
7. Click **SAVE TRANSACTION**

### Search Clients
1. In Client Management, enter search term
2. Click **SEARCH**
3. Results show matching clients by ID, name, or phone

### Update Client
1. Click on client in table
2. Edit details in form
3. Click **UPDATE**

### Delete Client
1. Select client from table
2. Click **DELETE**
3. Confirm deletion

## 🧮 Calculation Examples

### Example 1: Industrial Client - Full Service
**Client:** ABC Manufacturing Ltd (Industrial)

**Services:**
- Drilling: Symmetric, Depth: 150m
  - Down Payment: KSh 130,000
  - Depth Cost: (100 × 1,000) + (50 × 1,500) = KSh 175,000
  - **Total: KSh 305,000**

- Pump: Submersible Electric, Height: 10m
  - Base Cost: KSh 90,000
  - Height Cost: 10 × 1,000 = KSh 10,000
  - **Total: KSh 100,000**

- Tank: 5000L
  - **Cost: KSh 65,000**

- Plumbing: KSh 50,000

**Fees:**
- Survey Fee: KSh 20,000
- Authority Fee: KSh 50,000

**Calculation:**
```
Subtotal = 305,000 + 100,000 + 65,000 + 50,000 + 20,000 + 50,000
         = KSh 590,000

Tax (16%) = 590,000 × 0.16 = KSh 94,400

Grand Total = 590,000 + 94,400 = KSh 684,400
```

### Example 2: Domestic Client - Basic Service
**Client:** John Kamau (Domestic)

**Services:**
- Drilling: Symmetric, Depth: 80m
  - Down Payment: KSh 130,000
  - Depth Cost: 80 × 1,000 = KSh 80,000
  - **Total: KSh 210,000**

- Pump: Hand Pump
  - Base Cost: KSh 30,000
  - Height: 0m
  - **Total: KSh 30,000**

**Fees:**
- Survey Fee: KSh 7,000
- Authority Fee: KSh 10,000

**Calculation:**
```
Subtotal = 210,000 + 30,000 + 7,000 + 10,000 = KSh 257,000
Tax (16%) = 257,000 × 0.16 = KSh 41,120
Grand Total = 257,000 + 41,120 = KSh 298,120
```

## 📊 Database Schema Highlights

### Key Tables:
1. **users** - System users with roles
2. **clients** - Client information
3. **transactions** - Main service orders
4. **drilling_types** - Drilling service catalog
5. **pump_types** - Pump catalog
6. **tank_types** - Tank catalog
7. **survey_authority_fees** - Fee structure by category
8. **depth_height_charges** - Tiered pricing for depth/height
9. **plumbing_details** - Detailed plumbing records
10. **payments** - Payment tracking
11. **audit_log** - System audit trail

## 🎨 UI Design Features

- **Color-coded interface** for easy navigation
- **Real-time calculations** as you type
- **Form validation** prevents invalid data
- **Confirmation dialogs** for destructive actions
- **Search and filter** capabilities
- **Responsive layouts** for different screen sizes
- **Professional styling** with consistent theme

## 🔒 Security Features

- **Role-based access control** (Admin, Manager, Clerk)
- **Session management** prevents unauthorized access
- **Password authentication** (ready for BCrypt hashing)
- **Audit logging** tracks all changes
- **Connection pooling** for performance and security

## 📈 Future Enhancements (Not Yet Implemented)

The following modules are prepared but need completion:
- **Reports Module:** PDF/Excel generation
- **User Management:** Full CRUD for system users
- **Services Management:** Edit service prices
- **Payment Tracking:** Record payments, generate receipts
- **View Transactions:** Browse all transactions with filters
- **Dashboard Statistics:** Real-time stats from database
- **System Settings:** Configure company info, tax rates
- **Backup/Restore:** Database backup functionality

## 🐛 Troubleshooting

### Database Connection Failed
```
Error: Cannot connect to database
```
**Solution:**
1. Ensure MySQL is running: `sudo systemctl status mysql`
2. Check database exists: `mysql -u root -p -e "SHOW DATABASES;"`
3. Verify credentials in `DatabaseConnection.java`
4. Check firewall: `sudo ufw status`

### Maven Build Fails
```
Error: Could not resolve dependencies
```
**Solution:**
```bash
mvn clean
mvn dependency:purge-local-repository
mvn clean install
```

### JCalendar Not Found
```
Error: Package com.toedter.calendar does not exist
```
**Solution:**
```bash
mvn dependency:resolve
mvn clean install -U
```

### Application Won't Start
```
Error: ClassNotFoundException
```
**Solution:**
```bash
mvn clean package
java -cp target/uzima-borehole-system-1.0.0.jar com.uzima.Main
```

## 📞 Support & Contact

For issues or questions:
- Check the troubleshooting section above
- Review database logs: `/var/log/mysql/error.log`
- Check application logs in console output

## 📝 License

This project is developed for educational and commercial use by Uzima Borehole Company.

## 🙏 Acknowledgments

- Java Swing for UI framework
- MySQL for database management
- Maven for dependency management
- Open source community for libraries

---

**Version:** 1.0.0  
**Date:** January 21, 2026  
**Status:** Core modules implemented, additional features pending
