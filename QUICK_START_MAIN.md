# UZIMA APPLICATION - QUICK START GUIDE

## 🚀 How to Run the Application

### Prerequisites
- Java 11+ installed
- MySQL server running
- Maven installed (optional but recommended)

### Step 1: Database Setup
Ensure your database is set up with the correct schema:

```bash
# In MySQL:
mysql> USE uzima;
mysql> SOURCE src/main/resources/database_schema.sql;
```

### Step 2: Run the Application

#### Option A: Using IDE (Recommended)
1. Open `/home/kali/Desktop/UZIMABOREHOLE/uzima/src/main/java/com/uzima/Main.java`
2. Click the **Run** button (or press Shift+F10 in IntelliJ / Ctrl+Shift+F10 in VS Code)
3. The application window will open

#### Option B: Using Terminal
```bash
cd /home/kali/Desktop/UZIMABOREHOLE/uzima

# Compile
javac -cp src/main/java -d target/classes src/main/java/com/uzima/Main.java

# Run
java -cp target/classes:. com.uzima.Main
```

#### Option C: Using Maven
```bash
cd /home/kali/Desktop/UZIMABOREHOLE/uzima

# Clean and compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.uzima.Main"
```

## 📱 Application Screens

### 1. Login Screen
**First screen you'll see**
- Enter email and password
- Click "Sign In" button
- Demo account: `admin@uzima.com` / `password123`
- New users: Click "Don't have an account? Sign up" link

### 2. Sign Up Screen
**Create a new account**
- Fill in all fields (Name, Email, Phone, County, Service Category, Password)
- Passwords must be at least 6 characters
- Must match in both password fields
- Click "Create Account"
- You'll be automatically logged in and taken to Main Menu

### 3. Admin Dashboard
**For admin@uzima.com login**
- View all clients in table format
- See columns: ID, Name, Email, Phone, County, Category
- Click "Refresh" to reload client data
- Click "Logout" to return to login screen

### 4. Main Menu
**For regular client login**
- Shows 6 navigation options:
  - 📋 Request Service - Create new service request
  - 📄 View Invoices - View your invoices
  - 🔧 View Services - Browse services
  - 📊 View Reports - View reports
  - ⚙️ Settings - Update profile
  - 🚪 Logout - Sign out

## 🔑 Test Credentials

### Admin Account
```
Email: admin@uzima.com
Password: password123
Access: Admin Dashboard (view all clients)
```

### Create Your Own Account
1. Click "Sign up" on login screen
2. Fill in your details
3. Create password (min 6 characters)
4. Click "Create Account"
5. You'll be logged in automatically

## 📊 Project Architecture

```
Main.java (Entry Point)
├── CardLayout (4 screens)
│   ├── LOGIN PANEL
│   ├── SIGNUP PANEL
│   ├── ADMIN PANEL
│   └── MainMenuPanel (opens in new window)
│
└── Backend Services (via ClientService)
    ├── Authentication
    ├── Registration  
    ├── Data Retrieval
    └── Client Management
```

## 🔐 Security Features

- **Password Hashing**: SHA-256 with Base64 encoding
- **Input Validation**: Email format, password confirmation
- **Email Uniqueness**: Prevents duplicate accounts
- **Session Management**: Tracks logged-in client

## ⚙️ System Configuration

Edit `DatabaseConnection.java` if your database details differ:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/uzima";
private static final String DB_USER = "root";
private static final String DB_PASS = "your_password";
```

## 🐛 Troubleshooting

**Q: Connection Refused Error**
A: Make sure MySQL is running:
```bash
sudo service mysql start    # Linux
brew services start mysql   # Mac
# Windows: Start MySQL from Services
```

**Q: Database Not Found Error**
A: Create and import schema:
```bash
mysql -u root -p
mysql> CREATE DATABASE uzima;
mysql> USE uzima;
mysql> SOURCE database_schema.sql;
```

**Q: Login Failed**
A: Check your credentials:
- Email must be exact case (use admin@uzima.com)
- Password is case-sensitive
- New users must sign up first

**Q: Main Menu Doesn't Open**
A: Ensure MainMenuPanel.java exists in ui/ folder:
```bash
ls -la src/main/java/com/uzima/ui/MainMenuPanel.java
```

## 📝 File Locations

- **Main Application**: `/home/kali/Desktop/UZIMABOREHOLE/uzima/src/main/java/com/uzima/Main.java`
- **Database Schema**: `/home/kali/Desktop/UZIMABOREHOLE/uzima/src/main/resources/database_schema.sql`
- **Backend Services**: `/home/kali/Desktop/UZIMABOREHOLE/uzima/src/main/java/com/uzima/services/`
- **Models**: `/home/kali/Desktop/UZIMABOREHOLE/uzima/src/main/java/com/uzima/models/`
- **Database Layer**: `/home/kali/Desktop/UZIMABOREHOLE/uzima/src/main/java/com/uzima/database/`

## 🎯 What's Next?

1. ✅ Start the application
2. ✅ Test login with admin account
3. ✅ Create a new client account
4. ✅ Navigate the Main Menu
5. ✅ Explore service request features

## 📞 Support

If you encounter any issues:
1. Check the Troubleshooting section above
2. Verify database is running
3. Check console output for error messages
4. Review database schema matches expectations

---

**Application is ready to use!** 🚀
