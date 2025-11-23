# 🗄️ DATABASE SETUP GUIDE - JAVALEARNING

## ✅ Setup Complete!

Your `javalearning` database has been successfully configured with all required tables for the UZIMA application.

---

## 📋 Tables Created

### Core Tables
1. **clients** - Stores client information with password support
2. **drilling_services** - Available drilling service types
3. **pump_services** - Available pump service types
4. **plumbing_services** - Plumbing service details
5. **tank_installation** - Tank installation services
6. **depth_height_charges** - Depth-based drilling charges
7. **fees** - Survey and local authority fees

### Transaction Tables
8. **service_requests** - Client service requests
9. **invoices** - Billing and invoice records
10. **transactions** - Payment transaction tracking

---

## 🔑 Admin Account (Already Created)

```
Email:    admin@uzima.com
Password: password123
Access:   Full admin dashboard to view all clients
```

---

## 👥 Sample Test Accounts (Already Created)

### Account 1: Domestic Client
```
Full Name: John Doe
Email:     john@example.com
Phone:     0712345678
Address:   123 Main St
Category:  Domestic
Location:  Nairobi
```

### Account 2: Commercial Client
```
Full Name: Sarah Johnson
Email:     sarah@example.com
Phone:     0722345678
Address:   456 Oak Ave
Category:  Commercial
Location:  Mombasa
```

### Account 3: Industrial Client
```
Full Name: Mike Smith
Email:     mike@example.com
Phone:     0732345678
Address:   789 Pine Rd
Category:  Industrial
Location:  Kisumu
```

---

## 🔐 Database Connection Details

**File**: `src/main/java/com/uzima/database/DatabaseConnection.java`

```
Database:  javalearning
Host:      localhost
Port:      3306
User:      root
Password:  teddy
```

---

## ✅ Verification Commands

To verify the database is set up correctly, run these commands in MySQL:

### Check all tables
```sql
USE javalearning;
SHOW TABLES;
```

### Check clients table
```sql
SELECT * FROM clients;
```

### Check table structure
```sql
DESCRIBE clients;
```

### Count records
```sql
SELECT COUNT(*) FROM clients;
SELECT COUNT(*) FROM drilling_services;
SELECT COUNT(*) FROM pump_services;
SELECT COUNT(*) FROM fees;
```

---

## 🚀 Now You Can Run the Application

```bash
java -cp src/main/java com.uzima.Main
```

### Login Options:

1. **Admin Account** - See all clients in admin dashboard
   - Email: admin@uzima.com
   - Password: password123

2. **Test Account** - Access main menu
   - Email: john@example.com
   - Password: (same hash as admin)

3. **Create New Account** - Sign up with any email

---

## 📝 SQL Setup File

A complete setup SQL file has been created:
```
setup_javalearning_database.sql
```

If you need to rebuild the database, run:
```bash
mysql -u root -p javalearning < setup_javalearning_database.sql
```

---

## 🔧 Troubleshooting

### "Table 'javalearning.clients' doesn't exist"
**Solution**: Run the setup SQL file again
```bash
mysql -u root -p"teddy" javalearning < setup_javalearning_database.sql
```

### "Access denied for user 'root'@'localhost'"
**Solution**: Check password in DatabaseConnection.java (should be "teddy")

### Tables are empty
**Solution**: The setup script includes sample data. If empty, insert data:
```sql
INSERT INTO clients (full_name, email, phone, address, category, borehole_location, password) 
VALUES ('Test User', 'test@example.com', '0700000000', 'Test Address', 'Domestic', 'Nairobi', 'hashed_password');
```

---

## 📊 Database Schema Visualization

```
┌─────────────────────────────────────────────────┐
│                  JAVALEARNING DATABASE          │
├─────────────────────────────────────────────────┤
│                                                 │
│  CLIENTS (Core Table)                          │
│  ├─ client_id (PK)                             │
│  ├─ full_name                                  │
│  ├─ email (UNIQUE)                             │
│  ├─ phone                                      │
│  ├─ address                                    │
│  ├─ category (Enum)                            │
│  ├─ borehole_location                          │
│  ├─ password (SHA-256 hashed)                  │
│  └─ timestamps                                 │
│                                                 │
│  SERVICE REQUESTS (Relationship)                │
│  ├─ request_id (PK)                            │
│  ├─ client_id (FK → clients)                   │
│  ├─ drilling_id (FK → drilling_services)       │
│  ├─ pump_id (FK → pump_services)               │
│  ├─ status (Enum)                              │
│  └─ dates                                      │
│                                                 │
│  INVOICES (Billing)                            │
│  ├─ invoice_id (PK)                            │
│  ├─ request_id (FK)                            │
│  ├─ client_id (FK)                             │
│  ├─ costs breakdown                            │
│  ├─ total_cost                                 │
│  └─ payment_status                             │
│                                                 │
│  SERVICES (Configuration)                      │
│  ├─ drilling_services                          │
│  ├─ pump_services                              │
│  ├─ plumbing_services                          │
│  ├─ tank_installation                          │
│  ├─ fees                                       │
│  └─ depth_height_charges                       │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## 🎯 Next Steps

1. ✅ **Database Setup Complete** - Tables created
2. ✅ **Sample Data Inserted** - Test accounts ready
3. ✅ **Admin Account Ready** - admin@uzima.com
4. ➡️ **Run Application** - `java -cp src/main/java com.uzima.Main`
5. ➡️ **Test Login** - Use admin credentials
6. ➡️ **Explore Features** - Create accounts, request services

---

## 📞 Database Credentials

For reference, your database credentials are:

```
Hostname:  localhost
Database:  javalearning
Username:  root
Password:  teddy
Port:      3306
JDBC URL:  jdbc:mysql://localhost:3306/javalearning
```

**These are configured in**: `src/main/java/com/uzima/database/DatabaseConnection.java`

---

## ✨ You're Ready!

Your UZIMA application is now:
- ✅ Database configured
- ✅ Tables created
- ✅ Sample data loaded
- ✅ Admin account ready
- ✅ Ready to run!

**Start the application and login with:**
- Email: admin@uzima.com
- Password: password123

---

**🎊 Database setup is complete!** 🎊
