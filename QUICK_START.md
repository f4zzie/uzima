# QUICK SETUP GUIDE - UZIMA BOREHOLE SYSTEM

## 🚀 Quick Start (5 Minutes)

### Step 1: Start MySQL
```bash
sudo systemctl start mysql
```

### Step 2: Create Database
```bash
cd /home/kali/Desktop/uzima2/uzima
mysql -u root -p < database/uzima_db_setup.sql
```
**Enter your MySQL root password when prompted**

### Step 3: Update Database Password
Edit the file: `src/main/java/com/uzima/util/DatabaseConnection.java`

Change line:
```java
private static final String DB_PASSWORD = ""; 
```
To:
```java
private static final String DB_PASSWORD = "your_mysql_password";
```

### Step 4: Build and Run
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.uzima.Main"
```

### Step 5: Login
- **Username:** admin
- **Password:** admin123

## ✅ Verify Installation

### Check Database
```bash
mysql -u root -p -e "USE uzima_borehole_db; SELECT COUNT(*) FROM users;"
```
Expected output: `3` (three default users)

### Check Tables
```bash
mysql -u root -p uzima_borehole_db -e "SHOW TABLES;"
```
Expected output: 13 tables

## 🎯 Test the System

### 1. Register a Test Client
- Dashboard → **Manage Clients** → **NEW**
- Fill in required fields (marked with *)
- Click **SAVE**

### 2. Create a Test Transaction
- Dashboard → **New Transaction**
- Select the client you created
- Check **Include Drilling Service**
  - Select: Symmetric
  - Enter Depth: 100
- Click **CALCULATE TOTAL**
- Click **SAVE TRANSACTION**

### 3. Expected Results
For a Domestic client with:
- Symmetric drilling (100m depth)
- Calculation should show:
  - Down Payment: KSh 130,000
  - Depth Cost: KSh 100,000
  - Survey Fee: KSh 7,000
  - Authority Fee: KSh 10,000
  - Subtotal: KSh 247,000
  - Tax (16%): KSh 39,520
  - **Grand Total: KSh 286,520**

## 🔧 Common Issues

### "Cannot connect to database"
```bash
# Check if MySQL is running
sudo systemctl status mysql

# If not running, start it
sudo systemctl start mysql

# Check if you can login
mysql -u root -p
```

### "Database does not exist"
```bash
# Run the setup script again
mysql -u root -p < database/uzima_db_setup.sql
```

### "Maven not found"
```bash
# Install Maven
sudo apt update
sudo apt install maven

# Verify
mvn -version
```

### "Java version mismatch"
```bash
# Check Java version
java -version

# Should be 11 or higher
# If not, install:
sudo apt install openjdk-11-jdk
```

## 📋 Default User Accounts

| Username | Password    | Role    | Description |
|----------|-------------|---------|-------------|
| admin    | admin123    | ADMIN   | Full access |
| manager  | manager123  | MANAGER | Management  |
| clerk    | clerk123    | CLERK   | Data entry  |

## 🎨 Application Features to Test

1. ✅ Login with different users
2. ✅ Register a client (Industrial, Commercial, Domestic)
3. ✅ Search for clients
4. ✅ Update client information
5. ✅ Create transaction with drilling service
6. ✅ Create transaction with multiple services
7. ✅ Verify automatic calculations
8. ✅ Test depth-based pricing (enter 150m, 250m, 350m)

## 📞 Need Help?

Check the main README.md file for:
- Detailed documentation
- Calculation examples
- Full troubleshooting guide
- Database schema details

## 🎉 You're All Set!

The system is now ready to use. Explore the dashboard and test all features.

**Next Steps:**
1. Add real client data
2. Create actual transactions
3. Test different client categories
4. Verify all calculations
5. Explore reporting features (when implemented)

---

**Quick Reference:**
- Start MySQL: `sudo systemctl start mysql`
- Run App: `mvn exec:java -Dexec.mainClass="com.uzima.Main"`
- View Logs: `tail -f uzima-system.log`
- Database: `mysql -u root -p uzima_borehole_db`
